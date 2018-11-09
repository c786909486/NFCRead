package kanghong.axun.com.nfcdemo;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import static kanghong.axun.com.nfcdemo.ByteArrayChange.ByteArrayToHexString;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    private Button mRead;
    private Button mWrite;
    private TextView mShowText;

    //NFC适配器
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private String[][] mTechLists;
    private IntentFilter[] mFilters;
    private Tag tag;
    private byte[] code = MifareClassic.KEY_DEFAULT;


    private int bIndex;
    private int bCount;

    private TextView mClear;
//    private String info;
    private long id;
    private View view;
    private ShowPopup pop;
    static Handler uiHandler = null;
    private AsyncTask<Void, Void, String> nfcTask = null;
    private static  String appKey = "941c9b37d4dd4e569ff0320b21d9071c";

    private static String appSecret = "8eb5c020856040f7be7e52cff4ce3a77";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initNFC();
        initView();
        initDialog();
        pop =ShowPopup.getInstance(this);
        uiHandler = new MyHandler(this);
    }
    class MyHandler extends Handler {
        private MainActivity activity;

        MyHandler(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1000:
                    String msgTemp = (String) msg.obj;
                    Toast.makeText(MainActivity.this,msgTemp,Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter!=null){
            mAdapter.enableForegroundDispatch(this,mPendingIntent,mFilters,mTechLists);
        }
//        mManager.nfcEnable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter!=null){
            mAdapter.disableForegroundDispatch(this);
        }

//        mManager.nfcDisable();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String intentActionStr = intent.getAction();// 获取到本次启动的action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentActionStr)// NDEF类型
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentActionStr)// 其他类型
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intentActionStr)) {// 未知类型
            // 在intent中读取Tag id
            Log.d(TAG, intentActionStr);
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            byte[] bytesId = tag.getId();// 获取id数组
            String text = readShortTag(tag);
            String id16 = ByteArrayChange.bytesToHexString(bytesId);
            id = Long.parseLong(ByteArrayChange.bytesToHexString(bytesId), 16);
            Log.d(TAG,ByteArrayChange.bytesToHexString(bytesId));
            if (alertDialog.isShowing()){
                input.setText(id+"");
            }
            mShowText.setText("卡号"+id);

        }

        try {
            setIntent(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }




    private void initNFC() {
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        mFilters = new IntentFilter[] { ndef, };
        mTechLists = new String[][] { { IsoDep.class.getName() }, { NfcA.class.getName() }, };

        Log.d(" mTechLists", NfcF.class.getName() + mTechLists.length);

        if (mAdapter == null) {
            Toast.makeText(this, "设备不支持NFC！", Toast.LENGTH_LONG).show();
            return;
        }
        if (!mAdapter.isEnabled()) {
            Toast.makeText(this, "请在系统设置中先启用NFC功能！", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void initDialog(){
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        alertDialog = builder.create();
        view = LayoutInflater.from(this).inflate(R.layout.dialog_content,null);
        Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        Button applyBtn = (Button) view.findViewById(R.id.apply_btn);
        input = (EditText) view.findViewById(R.id.input_text);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input.getText().length()>0){
//
                    alertDialog.dismiss();
                }else {
                    Toast.makeText(MainActivity.this,"不可写入空数据",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void initView() {
        mRead = ((Button) findViewById(R.id.read_btn));
        mWrite = ((Button) findViewById(R.id.write_btn));
        mShowText = ((TextView) findViewById(R.id.show_text));
        mClear = ((TextView) findViewById(R.id.clear_btn));
        initClick();
    }

    private void initClick() {
        mRead.setOnClickListener(this);
        mWrite.setOnClickListener(this);
        mClear.setOnClickListener(this);
    }



    private StringBuilder dataInfo = new StringBuilder();

    //读取卡内长数据
    public String readTag(Tag tag) {
        if (tag == null){
            Toast.makeText(this,"与卡的连接已经断开",Toast.LENGTH_SHORT).show();
        }else {
            MifareClassic mfc = MifareClassic.get(tag);
            for (String tech : tag.getTechList()) {
                System.out.println(tech);// 显示设备支持技术
            }
            boolean auth = false;
            // 读取TAG

            try {
                // metaInfo.delete(0, metaInfo.length());//清空StringBuilder;
//                StringBuilder metaInfo = new StringBuilder();
                StringBuilder metaInfo = new StringBuilder();
                // Enable I/O operations to the tag from this TagTechnology object.
                mfc.connect();
                int type = mfc.getType();// 获取TAG的类型
                int sectorCount = mfc.getSectorCount();// 获取TAG中包含的扇区数
                String typeS = "";
                switch (type) {
                    case MifareClassic.TYPE_CLASSIC:
                        typeS = "TYPE_CLASSIC";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        typeS = "TYPE_PLUS";
                        break;
                    case MifareClassic.TYPE_PRO:
                        typeS = "TYPE_PRO";
                        break;
                    case MifareClassic.TYPE_UNKNOWN:
                        typeS = "TYPE_UNKNOWN";
                        break;

                }
                metaInfo.append("  卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                        + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize()
                        + "B\n");
                StringBuilder dataInfo = new StringBuilder();
                for (int j = 1; j < sectorCount; j++) {
                    // Authenticate a sector with key A.
                    auth = mfc.authenticateSectorWithKeyA(j,
                            MifareClassic.KEY_DEFAULT);// 逐个获取密码,MifareClassic.KEY_DEFAULT获取密码快成功，其余失败

                    if (auth) {
                        // 读取扇区中的块
                        bCount = mfc.getBlockCountInSector(j);
                        bIndex = mfc.sectorToBlock(j);
                        for (int i = 1; i < bCount; i++) {
                            byte[] data = mfc.readBlock(bIndex);
//                            String dataInfo = ByteArrayChange.ByteArrayToHexString(data);
                            dataInfo.append( ByteArrayToHexString(data));

//                        String temp = "块"+(j)+"里的内容是："+dataInfo;
//                            Log.d(TAG, "readTag: " + dataInfo);

                            bIndex++;
                        }

                    } else {
                        metaInfo.append("Sector " + j + ":验证失败\n");
                    }
                }
                String temp = ToStringHex.decode(dataInfo.toString());
                metaInfo.append("卡内数据 : "
                        + temp
                        + "\n");
                return metaInfo.toString();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                if (mfc != null) {
                    try {
                        mfc.close();
                    } catch (IOException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        }
        return null;

    }

     //读取数据
    public String readShortTag(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);

        for (String tech : tag.getTechList()) {
            System.out.println(tech);// 显示设备支持技术
        }
        boolean auth = false;
        // 读取TAG

        try {
            // metaInfo.delete(0, metaInfo.length());//清空StringBuilder;
            StringBuilder metaInfo = new StringBuilder();
            // Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();
            int type = mfc.getType();// 获取TAG的类型
            int sectorCount = mfc.getSectorCount();// 获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    break;

            }
            metaInfo.append("  卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize()
                    + "B\n");
            for (int j = 1; j < sectorCount; j++) {
                // Authenticate a sector with key A.
                auth = mfc.authenticateSectorWithKeyB(j,
                        code);// 逐个获取密码
                /*
                 * byte[]
                 * codeByte_Default=MifareClassic.KEY_DEFAULT;//FFFFFFFFFFFF
                 * byte[]
                 * codeByte_Directory=MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY
                 * ;//A0A1A2A3A4A5 byte[]
                 * codeByte_Forum=MifareClassic.KEY_NFC_FORUM;//D3F7D3F7D3F7
                 */if (auth) {
                    metaInfo.append("Sector " + j + ":验证成功\n");
                    // 读取扇区中的块
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 1; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo.append("Block " + bIndex + " : "
                                + ByteArrayToHexString(data)
                                + "\n");
                        bIndex++;
                    }
                    Log.d(TAG, "readShortTag: "+metaInfo.toString());
                } else {
                    metaInfo.append("Sector " + j + ":验证失败\n");
                }

            }
            return metaInfo.toString();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (mfc != null) {
                try {
                    mfc.close();
                } catch (IOException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
        return null;

    }

    // 写数据
//    public void writeTag(Tag tag, String str) {
//        MifareClassic mfc = MifareClassic.get(tag);
//
//        try {
//            if (mfc != null) {
//                mfc.connect();
//            } else {
//                Toast.makeText(this, "写入失败", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            Log.i("write", "----connect-------------");
//            boolean CodeAuth = false;
//            byte[] b1 = str.getBytes();
//            Log.d(TAG, "输入字符长度："+b1.length);
//            if (b1.length <= 720) {
//                //System.out.println("------b1.length:" + b1.length);
//                int num = b1.length / 16;
//                Log.d(TAG, "num= " + num);
//                int next = b1.length / 48 + 1;
//                Log.d(TAG, "扇区next的值为" + next);
//                b0 = new byte[16];
//                if (!(b1.length % 16 == 0)) {
//                    for (int i = 1, j = 1; i <= num; i++) {
//                        CodeAuth = mfc.authenticateSectorWithKeyA(j, code);
//                        System.arraycopy(b1, 16 * (i - 1), b0, 0, 16);
//                        mfc.writeBlock(block[i - 1], b0);
//                        if (i % 3 == 0) {
//                            j++;
//                        }
//                    }
//                    //Log.d("下一个模块", "测试");
//                    CodeAuth = mfc.authenticateSectorWithKeyA(next,// 非常重要------
//                            code);
//                    //Log.d("获取第5块的密码", "---成功-------");
//                    byte[] b2 = { 0 };
//                    b0 = new byte[16];
//                    System.arraycopy(b1, 16 * num, b0, 0, b1.length % 16);
//                    System.arraycopy(b2, 0, b0, b1.length % 16, b2.length);
//                    mfc.writeBlock(block[num], b0);
//                    mfc.close();
//                    Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
//                    return;
//                } else {
//                    for (int i = 1, j = 1; i <= num; i++) {
//                        if (i % 3 == 0) {
//                            j++;
//                            System.out.println("扇区j的值为：" + j);
//                        }
//                        CodeAuth = mfc.authenticateSectorWithKeyA(j,// 非常重要---------
//                                code);
//                        System.arraycopy(b1, 16 * (i - 1), b0, 0, 16);
//                        mfc.writeBlock(block[i - 1], b0);
//                        str += ByteArrayToHexString(b0);
//                        System.out.println("Block" + i + ": " + str);
//                    }
//                    mfc.close();
//                    Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//            } else {
//                Toast.makeText(getBaseContext(), "字符过长，内存不足", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } finally {
//            try {
//                mfc.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.read_btn:
//                showDialog();
//                String content = readTag(tag);
                showDialog();
                break;
            case R.id.write_btn:

                break;
            case R.id.clear_btn:

                break;
        }
    }

    private EditText input;
    private AlertDialog alertDialog;
    private void showDialog(){

        alertDialog.show();
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        alertDialog.getWindow().setContentView(view);

        if (mAdapter!=null){
            mAdapter.enableForegroundDispatch(this,mPendingIntent,mFilters,mTechLists);
        }

    }

    private void showInnerDialog(final AlertDialog alertDialog){
        new AlertDialog.Builder(this).setTitle("再次确认").setMessage("再次确认").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //提交
                dialog.dismiss();
                alertDialog.dismiss();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }

    private void showRemain(){
        View view = LayoutInflater.from(this).inflate(R.layout.pop_remain,null);
    }
}
