package com.vinh.moneymanager.room;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.vinh.moneymanager.libs.Helper;
import com.vinh.moneymanager.room.daos.AccountDao;
import com.vinh.moneymanager.room.daos.CategoryDao;
import com.vinh.moneymanager.room.daos.FinanceDao;
import com.vinh.moneymanager.room.daos.TransferDao;
import com.vinh.moneymanager.room.daos.TypeDao;
import com.vinh.moneymanager.room.entities.Account;
import com.vinh.moneymanager.room.entities.Category;
import com.vinh.moneymanager.room.entities.Finance;
import com.vinh.moneymanager.room.entities.Transfer;
import com.vinh.moneymanager.room.entities.Type;

@Database(entities = {Type.class, Category.class, Finance.class, Account.class, Transfer.class}, version = 1)
public abstract class MoneyManagerDatabase extends RoomDatabase {

    private static MoneyManagerDatabase instance;
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDBAsyncTask(instance).execute();
        }
    };

    public static synchronized MoneyManagerDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MoneyManagerDatabase.class, "money_manager")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    public abstract TypeDao typeDao();

    public abstract CategoryDao categoryDao();

    public abstract AccountDao accountDao();

    public abstract FinanceDao financeDao();

    public abstract TransferDao transferDao();

    private static class PopulateDBAsyncTask extends AsyncTask<Void, Void, Void> {

        private final TypeDao typeDao;
        private final CategoryDao categoryDao;
        private final FinanceDao financeDao;
        private final AccountDao accountDao;

        private PopulateDBAsyncTask(MoneyManagerDatabase database) {
            typeDao = database.typeDao();
            categoryDao = database.categoryDao();
            financeDao = database.financeDao();
            accountDao = database.accountDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            typeDao.insert(new Type(Helper.TYPE_INCOME, "Thu nh???p"));
            typeDao.insert(new Type(Helper.TYPE_EXPENSE, "Chi ti??u"));
            typeDao.insert(new Type(Helper.TYPE_TRANSFER, "Chuy???n kho???n"));

            categoryDao.insert(new Category("??n u???ng", Helper.TYPE_EXPENSE, "Bao g???m ??n v???t v?? ??n b???a ch??nh", 0));
            categoryDao.insert(new Category("Mua s???m", Helper.TYPE_EXPENSE, "Mua c??c v???t d???ng c?? nh??n", 1));

            categoryDao.insert(new Category("L????ng", Helper.TYPE_INCOME, "Mua c??c v???t d???ng c?? nh??n", 0));

            accountDao.insert(new Account("Ti???n m???t", 5000000, "Ti???n m???t", 1));
            accountDao.insert(new Account("Th??? BIDV", 8000000, "Ti???n trong th??? ng??n h??ng", 0));

            return null;
        }
    }
}
