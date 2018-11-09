package kanghong.axun.com.nfcdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import static kanghong.axun.com.nfcdemo.ByteArrayChange.ByteArrayToHexString;

/**
 * Created by Administrator on 2017/12/13.
 */

public class NFCManager {
    public static final String TAG = "NFCManager";
    private Activity mActivity;
    //NFC适配器
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private String[][] mTechLists;
    private IntentFilter[] mFilters;
    private Tag tag;
    private byte[] code = MifareClassic.KEY_DEFAULT;

    private int mCount = 0;
    int block[] = { 4, 5, 6, 8, 9, 10, 12, 13, 14, 16, 17, 18, 20, 21, 22, 24,
            25, 26, 28, 29, 30, 32, 33, 34, 36, 37, 38, 40, 41, 42, 44, 45, 46,
            48, 49, 50, 52, 53, 54, 56, 57, 58, 60, 61, 62 };
    private byte[] data3, b0;
    String info = "";
    private int bIndex;
    private int bCount;
    private int BlockData;

    public NFCManager(Activity mActivity) {
        this.mActivity = mActivity;
        initNFC(mActivity);
    }

    private void initNFC(Activity mActivity) {
        mAdapter = NfcAdapter.getDefaultAdapter(mActivity);
        mPendingIntent = PendingIntent.getActivity(mActivity, 0,
                new Intent(mActivity, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
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
            Toast.makeText(mActivity, "设备不支持NFC！", Toast.LENGTH_LONG).show();
//            finish();
            return;
        }
        if (!mAdapter.isEnabled()) {
            Toast.makeText(mActivity, "请在系统设置中先启用NFC功能！", Toast.LENGTH_LONG).show();
//            finish();
            return;
        }
    }

    private StringBuilder dataInfo = new StringBuilder();

    //读取卡内长数据
    public String readTag(Tag tag) {
        if (tag == null){
            Toast.makeText(mActivity,"与卡的连接已经断开",Toast.LENGTH_SHORT).show();
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
                // Enable I/O operations to the tag from mActivity TagTechnology object.
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
                Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                if (mfc != null) {
                    try {
                        mfc.close();
                    } catch (IOException e) {
                        Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        }
        return null;

    }

    // //读取数据
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
            // Enable I/O operations to the tag from mActivity TagTechnology object.
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
                auth = mfc.authenticateSectorWithKeyA(j,
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
            Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (mfc != null) {
                try {
                    mfc.close();
                } catch (IOException e) {
                    Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
        return null;

    }

    // 写数据
    public void writeTag(String str) {
        MifareClassic mfc = MifareClassic.get(tag);

        try {
            if (mfc != null) {
                mfc.connect();
            } else {
                Toast.makeText(mActivity, "写入失败", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i("write", "----connect-------------");
            boolean CodeAuth = false;
            byte[] b1 = str.getBytes();
            Log.d(TAG, "输入字符长度："+b1.length);
            if (b1.length <= 720) {
                //System.out.println("------b1.length:" + b1.length);
                int num = b1.length / 16;
                Log.d(TAG, "num= " + num);
                int next = b1.length / 48 + 1;
                Log.d(TAG, "扇区next的值为" + next);
                b0 = new byte[16];
                if (!(b1.length % 16 == 0)) {
                    for (int i = 1, j = 1; i <= num; i++) {
                        CodeAuth = mfc.authenticateSectorWithKeyA(j, code);
                        System.arraycopy(b1, 16 * (i - 1), b0, 0, 16);
                        mfc.writeBlock(block[i - 1], b0);
                        if (i % 3 == 0) {
                            j++;
                        }
                    }
                    //Log.d("下一个模块", "测试");
                    CodeAuth = mfc.authenticateSectorWithKeyA(next,// 非常重要------
                            code);
                    //Log.d("获取第5块的密码", "---成功-------");
                    byte[] b2 = { 0 };
                    b0 = new byte[16];
                    System.arraycopy(b1, 16 * num, b0, 0, b1.length % 16);
                    System.arraycopy(b2, 0, b0, b1.length % 16, b2.length);
                    mfc.writeBlock(block[num], b0);
                    mfc.close();
                    Toast.makeText(mActivity, "写入成功", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    for (int i = 1, j = 1; i <= num; i++) {
                        if (i % 3 == 0) {
                            j++;
                            System.out.println("扇区j的值为：" + j);
                        }
                        CodeAuth = mfc.authenticateSectorWithKeyA(j,// 非常重要---------
                                code);
                        System.arraycopy(b1, 16 * (i - 1), b0, 0, 16);
                        mfc.writeBlock(block[i - 1], b0);
                        str += ByteArrayToHexString(b0);
                        System.out.println("Block" + i + ": " + str);
                    }
                    mfc.close();
                    Toast.makeText(mActivity, "写入成功", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(mActivity, "字符过长，内存不足", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                mfc.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private String cardId="";
    //获取卡id
    public String getID(Intent intent){
        String intentActionStr = intent.getAction();// 获取到本次启动的action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentActionStr)// NDEF类型
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentActionStr)// 其他类型
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intentActionStr)) {// 未知类型
            // 在intent中读取Tag id
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] bytesId = tag.getId();// 获取id数组
            long id = Long.parseLong(ByteArrayChange.bytesToHexString(bytesId),16);
            cardId = id+"";
        }
        return cardId;
    }
    //获取卡内数据
    public String getContent(Intent intent){
        String intentActionStr = intent.getAction();// 获取到本次启动的action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentActionStr)// NDEF类型
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentActionStr)// 其他类型
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intentActionStr)) {// 未知类型
            // 在intent中读取Tag id
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] bytesId = tag.getId();// 获取id数组
            long id = Long.parseLong(ByteArrayChange.bytesToHexString(bytesId),16);
            info +=  + id+"\n";
            info += readShortTag(tag)+"\n";
            Log.d(TAG, "onNewIntent: "+info);
        }
        return info;
    }

    public void nfcEnable(){
        if (mAdapter!=null){
            mAdapter.enableForegroundDispatch(mActivity,mPendingIntent,mFilters,mTechLists);
        }
       
    }
    public void nfcDisable(){
        if (mAdapter!=null){
            mAdapter.disableForegroundDispatch(mActivity);
        }
    }

    public Tag getTag(){
        return tag;
    }

}
