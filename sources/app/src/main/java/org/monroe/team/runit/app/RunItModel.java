package org.monroe.team.runit.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;

import org.monroe.team.android.box.db.DAOFactory;
import org.monroe.team.android.box.db.DAOSupport;
import org.monroe.team.android.box.db.DBHelper;
import org.monroe.team.android.box.db.Schema;
import org.monroe.team.android.box.db.TransactionManager;
import org.monroe.team.android.box.manager.NetworkManager;
import org.monroe.team.android.box.manager.ServiceRegistry;
import org.monroe.team.android.box.manager.Model;
import org.monroe.team.runit.app.db.Dao;
import org.monroe.team.runit.app.db.RunitSchema;
import org.monroe.team.runit.app.service.ApplicationRegistry;
import org.monroe.team.runit.app.service.CategoryNameResolver;
import org.monroe.team.runit.app.service.PlayMarketDetailsProvider;

public class RunItModel extends Model {

    public RunItModel(String appName, Context context) {
        super(appName, context);
    }

    @Override
    protected void constructor(String appName, Context context, ServiceRegistry serviceRegistry) {

        serviceRegistry.registrate(PackageManager.class, context.getPackageManager());
        serviceRegistry.registrate(ApplicationRegistry.class, new ApplicationRegistry(usingService(PackageManager.class)));
        serviceRegistry.registrate(NetworkManager.class, new NetworkManager(context));
        serviceRegistry.registrate(PlayMarketDetailsProvider.class, new PlayMarketDetailsProvider());
        serviceRegistry.registrate(CategoryNameResolver.class, new CategoryNameResolver(context));

        final RunitSchema schema = new RunitSchema();
        DBHelper helper = new DBHelper(context, schema);
        TransactionManager transactionManager = new TransactionManager(helper, new DAOFactory() {
            @Override
            public DAOSupport createInstanceFor(SQLiteDatabase database) {
                return new Dao(database, schema);
            }
        });
        serviceRegistry.registrate(TransactionManager.class, transactionManager);
    }

}
