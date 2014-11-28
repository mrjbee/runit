package org.monroe.team.android.box.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BackgroundTaskManager {

    private final ExecutorService taskExecutionService = Executors.newCachedThreadPool();
    private final ExecutorService notificationExecutionService = Executors.newCachedThreadPool();
    private Future notificationTaskFuture;
    private List<BackgroundTask> awaitingTaskList = new ArrayList<BackgroundTask>();

    final public synchronized <ResultType> BackgroundTask<ResultType> execute(
            Callable<ResultType> callable) {
        return execute(callable, null);
    }

    final public synchronized <ResultType> BackgroundTask<ResultType> execute(
                                Callable<ResultType> callable,
                                TaskCompletionNotificationObserver<ResultType> observer) {

        Future<ResultType> future = taskExecutionService.submit(callable);
        BackgroundTask<ResultType> answer = new BackgroundTask<ResultType>(future,observer);
        if (observer != null)
            scheduleResultCheck(answer);
        return answer;

    }

    private <ResultType> void scheduleResultCheck(BackgroundTask<ResultType> answer) {
        markBackgroundTask(answer);
        if (notificationTaskFuture == null || notificationTaskFuture.isDone()){
            notificationTaskFuture = taskExecutionService.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    while (!Thread.currentThread().isInterrupted() && awaitingTaskList != null){
                        List<BackgroundTask> awaitingTaskSnapshot = new ArrayList<BackgroundTask>(awaitingTaskList);
                        List<BackgroundTask> tasksToRemoveList = new ArrayList<BackgroundTask>();
                        for (BackgroundTask backgroundTask : awaitingTaskSnapshot) {
                            try {
                                Object result = backgroundTask.actualFuture.get(100, TimeUnit.MILLISECONDS);
                                tasksToRemoveList.add(backgroundTask);
                                backgroundTask.observer.onSuccess(result);
                            }
                            catch (TimeoutException e){/*do nothing just go to next*/}
                            catch (Exception e){
                                backgroundTask.observer.onFails(e);
                                tasksToRemoveList.add(backgroundTask);
                            }
                        }
                        for (BackgroundTask backgroundTask : tasksToRemoveList) {
                            unmarkBackgroundTask(backgroundTask);
                        }
                    }
                    return null;
                }
            });
        }
    }

    private synchronized void markBackgroundTask(BackgroundTask backgroundTask) {
        awaitingTaskList.add(backgroundTask);
    }

    private synchronized void unmarkBackgroundTask(BackgroundTask backgroundTask) {
        awaitingTaskList.remove(backgroundTask);
    }

    public static class BackgroundTask <Result> {

        private final Future<Result> actualFuture;
        private final TaskCompletionNotificationObserver<Result> observer;

        public BackgroundTask(Future<Result> actualFuture, TaskCompletionNotificationObserver<Result> observer) {
            this.actualFuture = actualFuture;
            this.observer = observer;
        }

        public void cancel() {
            actualFuture.cancel(true);
        }
    }

    public static interface TaskCompletionNotificationObserver<Result>{
        public void onSuccess(Result result);
        public void onFails(Exception e);
    }

}
