package com.vinh.moneymanager.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.vinh.moneymanager.R;
import com.vinh.moneymanager.databinding.ActivityAddEditAccountBinding;
import com.vinh.moneymanager.libs.Helper;
import com.vinh.moneymanager.room.entities.Account;
import com.vinh.moneymanager.room.entities.Finance;
import com.vinh.moneymanager.room.entities.Transfer;
import com.vinh.moneymanager.viewmodels.AccountViewModel;
import com.vinh.moneymanager.viewmodels.AddEditAccountViewModel;
import com.vinh.moneymanager.viewmodels.FinanceViewModel;
import com.vinh.moneymanager.viewmodels.TransferViewModel;

import java.util.List;

import static com.vinh.moneymanager.libs.Helper.iconsAccount;

public class AddEditAccountActivity extends AppCompatActivity {

    private AddEditAccountViewModel mViewModel;
    private AccountViewModel accountViewModel;
    private FinanceViewModel financeViewModel;
    private TransferViewModel transferViewModel;

    private HandlerClick handler;

    private List<Account> allAccounts;
    private List<Finance> allFinances;
    private List<Transfer> allTransfers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAddEditAccountBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_add_edit_account);

        setupToolbar();

        mViewModel = new AddEditAccountViewModel();

        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        financeViewModel = new ViewModelProvider(this).get(FinanceViewModel.class);
        transferViewModel = new ViewModelProvider(this).get(TransferViewModel.class);

        accountViewModel.getAccounts().observe(this, accounts -> allAccounts = accounts);
        financeViewModel.getAllFinances().observe(this, finances -> allFinances = finances);
        transferViewModel.getTransfers().observe(this, transfers -> allTransfers = transfers);

        // Get data from intent
        getData();

        // Binding d??? li???u ?????n layout ????? hi???n th???
        binding.setViewModel(mViewModel);

        // C??i ?????t click
        handler = new HandlerClick();
        binding.setHandler(handler);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.TitleFont);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
    }

    /**
     * Th???c hi???n h??nh ?????ng th??m/ch???nh s???a t??i kho???n
     */
    private void submit() {
        if (isAccountNameValid()) {
            Account account = mViewModel.getAccount();

            if (account.getAccountId() == 0) {
                // Th??m account m???i
                accountViewModel.insert(account);
                System.out.println("Th??m account th??nh c??ng!");
            } else {
                // Update account
                accountViewModel.update(account);
                System.out.println("Update account th??nh c??ng!");
            }
            finish();   // ????ng activity
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.finance_activity_menu, menu);

        // ???n n??t delete khi
        if (!getIntent().hasExtra(Helper.EDIT_ACCOUNT)) {
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_delete:
                deleteAccount();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Hi???n th??? dialog x??c nh???n x??a t??i kho???n
     */
    private void deleteAccount() {
        Account account = mViewModel.getAccount();
        if (canDelete(account.getAccountId())) {

            // T???o 1 dialog x??c nh???n
            new AlertDialog.Builder(this)
                    .setTitle("X??c nh???n x??a")
                    .setMessage("B???n c?? x??c ?????nh mu???n x??a t??i kho???n n??y?")
                    .setPositiveButton("X??A", (dialog, which) -> {
                        accountViewModel.delete(account);
                        dialog.cancel();
                        Log.d("MMM", "Account Deleted");
                        finish();
                    }).setNegativeButton("H???Y", (dialog, which) -> {
                dialog.cancel();
            }).show();

        } else {
            Toast.makeText(this, "T??i kho???n ???? ph??t sinh giao d???ch n??n kh??ng th??? x??a!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Ki???m tra xem t??i kho???n hi???n t???i c?? th??? x??a hay kh??ng
     *
     * @param id id t??i kho???n mu???n ki???m tra
     * @return c?? th??? x??a hay kh??ng
     */
    private boolean canDelete(int id) {
        for (Finance f : allFinances) {
            // N???u t??i kho???n ???? c?? ph??t sinh giao d???ch thu/chi => Kh??ng th??? x??a
            if (f.getAccountId() == id) return false;
        }
        for (Transfer t : allTransfers) {
            // N???u t??i kho???n ???? c?? ph??t sinh chuy???n kho???n => Kh??ng th??? x??a
            if (t.getAccountInId() == id || t.getAccountOutId() == id) return false;
        }
        return true;
    }

    /**
     * L???y data t??? Intent ???????c g???i t??? MainActivity
     * Bao g???m c??c th??ng tin c???n thi???t c???a 1 t??i kho???n
     * Nh???m hi???n th??? d??? li???u l??n m??n h??nh
     */
    private void getData() {
        if (getIntent().hasExtra(Helper.EDIT_ACCOUNT)) {
            Bundle data = getIntent().getBundleExtra(Helper.EDIT_ACCOUNT);

            int accountId = data.getInt(Helper.ACCOUNT_ID, 0);
            String accountName = data.getString(Helper.ACCOUNT_NAME);
            long balance = data.getLong(Helper.ACCOUNT_BALANCE, 0);
            String desc = data.getString(Helper.ACCOUNT_DESCRIPTION);
            int iconIndex = data.getInt(Helper.ACCOUNT_ICON, 0);

            Account account = new Account(accountName, balance, desc, iconIndex);
            account.setAccountId(accountId);

            mViewModel.setAccount(account);

            getSupportActionBar().setTitle("Ch???nh s???a t??i kho???n");
            mViewModel.setButtonText("Ch???nh s???a");
        } else {
            getSupportActionBar().setTitle("Th??m t??i kho???n");
            mViewModel.setButtonText("Th??m");
        }
    }

    /**
     * Ki???m tra t??n t??i kho???n c?? h???p l??? hay kh??ng?
     *
     * @return
     */
    private boolean isAccountNameValid() {
        String name = mViewModel.getAccountName();
        if (name == null || name.isEmpty()) {
            Toast.makeText(this, "T??n t??i kho???n kh??ng ???????c b??? tr???ng!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (isAccountNameExists(name, mViewModel.getAccount().getAccountId())) {
            Toast.makeText(this, "T??n t??i kho???n b??? tr??ng!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Ki???m tra t??n t??i kho???n ???? t???n t???i hay ch??a
     *
     * @param accountName T??n t??i kho???n
     * @param ignoreId    ID t??i kho???n b??? qua khi ki???m tra (b??? qua tr?????ng h???p t??? so s??nh v???i ch??nh n??)
     * @return
     */
    private boolean isAccountNameExists(String accountName, int ignoreId) {
        if (allAccounts == null) return false;

        for (Account a : allAccounts) {
            if (a.getAccountId() != ignoreId && a.getAccountName().equals(accountName)) {
                return true;
            }
        }

        return false;
    }


    /**
     * L???p c??i ?????t s??? ki???n click, bao g???m:
     * Hi???n th??? dialog ch???n icon t??i kho???n
     * X??c nh???n th??m/s???a t??i kho???n
     */
    public class HandlerClick {
        private final Dialog dialog;

        public HandlerClick() {
            dialog = new Dialog(AddEditAccountActivity.this);
            dialog.setContentView(R.layout.dialog_icon);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            ImageView imgClose = dialog.findViewById(R.id.img_close_dialog);
            imgClose.setOnClickListener((v) -> dialog.cancel());

            GridView gridView = dialog.findViewById(R.id.grid_view_icon);
            gridView.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return iconsAccount.length;
                }

                @Override
                public Integer getItem(int position) {
                    return iconsAccount[position];
                }

                @Override
                public long getItemId(int position) {
                    return iconsAccount[position];
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = LayoutInflater.from(AddEditAccountActivity.this).inflate(R.layout.single_image, null);
                        ImageView imageView = convertView.findViewById(R.id.img_view_icon);
                        imageView.setImageResource(getItem(position));
                    }
                    return convertView;
                }
            });

            gridView.setOnItemClickListener((parent, view, position, id) -> {
                mViewModel.setIcon(position);
                dialog.cancel();
            });
        }

        public void submitAccount() {
            submit();
        }

        public void showDialogSelectIcon() {
            dialog.show();
        }
    }

}