package net.common.handler;

import net.common.tool.ShareMemoryIniter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Properties;

/**
 * Created by hanlia on 2017/1/6.
 * 共享内存块
 * 该内存块在每个连接建立时分配
 * 主要用来做该连接的相关计数
 * 在实例化一个ShareMemory时一般使用sourceID@goalID或goalID@sourceID作为shareFileName
 * 并规定共享文件的某一段为某个计数器
 */
public class ShareMemory {
    private int memorySize = 1024*1024;                    //开辟共享内存大小
    private int fsize = 0;                                                  //文件的实际大小
    private String shareFileName;                                 //共享内存文件名
    private String sharePath;                                          //共享内存路径
    private MappedByteBuffer mapBuf = null;              //定义共享内存缓冲区
    private FileChannel fchannel = null;                        //定义相应的文件通道
    private FileLock flock = null;                                      //定义文件区域锁定的标记。
    private Properties props = null;
    private RandomAccessFile RAFile = null;                 //定义一个随机存取文件对象
    private boolean initFlag=false;

    public ShareMemory(String shareFileName, String sharePath) {
        if (sharePath.length()==0 || sharePath.equals("default")) {
            this.sharePath = System.getProperty("user.dir") + "\\src\\net\\common\\tool\\share\\";
        } else { this.sharePath = sharePath;}
        if(shareFileName.length()==0 || shareFileName.equals("default")){
            this.shareFileName="shareMemory";
        } else{ this.shareFileName = shareFileName; }
        try {
            RAFile = new RandomAccessFile(this.sharePath + this.shareFileName + ".sm", "rw");
            fchannel = RAFile.getChannel();
            fsize = (int) fchannel.size();
            if (fsize < memorySize) {
                byte bb[] = new byte[memorySize - fsize];
                ByteBuffer bf = ByteBuffer.wrap(bb);
                bf.clear();
                fchannel.position(fsize);
                fchannel.write(bf);
                fchannel.force(false);
                fsize = memorySize;
            }
            mapBuf = fchannel.map(FileChannel.MapMode.READ_WRITE, 0, fsize);
        } catch (IOException e) { e.printStackTrace();}
        this.initFlag=false;
    }

    public boolean isInitFlag(){return initFlag;}
    public void setInitFlag(boolean flag){this.initFlag=flag;}
    /**
     * 读取方法实现
     * 由于共享内存的读写均需要加锁
     * 因此需要定义加锁的起始位置lockStart和加锁长度lockLen
     * cacheMessageByteBuf为待写入消息
     * 在实现时首先使用ServerCacheTransfer将BaseProtocol.BaseMessage转化成byte[]
     * 同样在重发时需要读取cacheMessageByteBuf并转化成BaseMessage
     * @param lockStart        锁定区域开始的位置；必须为非负数
     * @param lockLen       锁定区域的大小；必须为非负数
     * @param cacheMessageByteBuf    待写入数据
     * @return
     */
    public synchronized int write(int lockStart, int lockLen, byte[] cacheMessageByteBuf) {
        if (lockStart >= fsize || lockStart + lockLen >= fsize) { return 0;}
        FileLock flock = null;
        try {
            flock = fchannel.lock(lockStart, lockLen, false);
            if (flock != null) {
                mapBuf.position(lockStart);
                ByteBuffer bf1 = ByteBuffer.wrap(cacheMessageByteBuf);
                mapBuf.put(bf1);
                flock.release();
                return lockLen;
            }
        } catch (Exception e) {
            if (flock != null) {
                try { flock.release();} catch (IOException e1) { e1.printStackTrace();}
            }
            return 0;
        }
        return 0;
    }

    /**
     * 读取共享内存缓存的消息
     * @param lockStart        锁定区域开始的位置；必须为非负数
     * @param lockLen       锁定区域的大小；必须为非负数
     * @param cacheMessageByteBuf     要取的数据
     * @return
     */
    public synchronized int read(int lockStart, int lockLen, byte[] cacheMessageByteBuf) {
        if (lockStart >= fsize) { return 0; }
        FileLock flock = null;
        try {
            flock = fchannel.lock(lockStart, lockStart, false);
            if (flock != null) {
                mapBuf.position(lockStart);
                if (mapBuf.remaining() < lockLen) { lockLen = mapBuf.remaining();}
                if (lockLen > 0) { mapBuf.get(cacheMessageByteBuf, 0, lockLen); }
                flock.release();
                return lockLen;
            }
        } catch (Exception e) {
            if (flock != null) {
                try { flock.release();
                } catch (IOException e1) { e1.printStackTrace();}
            }
            return 0;
        }
        return 0;
    }

    /**
     * 完成，关闭相关操作
     */
    protected void finalize() throws Throwable {
        if (fchannel != null) {try {fchannel.close();
        } catch (IOException e) {e.printStackTrace();}
            fchannel = null;
        }
        if (RAFile != null) { try { RAFile.close();
        } catch (IOException e) {e.printStackTrace();}
            RAFile = null;
        }
        mapBuf = null;
    }

    /**
     * 关闭共享内存操作
     */
    public synchronized void closeSMFile() {
        if (fchannel != null) {
            try {fchannel.close();
            } catch (IOException e) {e.printStackTrace();}
            fchannel = null;
        }
        if (RAFile != null) {
            try {RAFile.close();
            } catch (IOException e) {e.printStackTrace();
            }
            RAFile = null;
        }
        mapBuf = null;
    }

    /**
     *  检查退出
     * @return  true-成功，false-失败
     */
    public synchronized boolean checkToExit() {
        byte bb[] = new byte[1];
        if (read(1, 1, bb) > 0) {
            if (bb[0] == 1) {return true;}
        }
        return false;
    }
    /**
     * 复位退出
     */
    public synchronized void resetExit() {
        byte bb[] = new byte[1];
        bb[0] = 0;
        write(1, 1, bb);
    }
    /**
     * 退出
     */
    public synchronized void toExit() {
        byte bb[] = new byte[1];
        bb[0] = 1;
        write(1, 1, bb);
    }

    public  String getSharePath(){return sharePath;}

    public static void main(String arsg[]) throws Exception{
        ShareMemory sm = new ShareMemory("","");
        ShareMemory sm1=new ShareMemory("","");
        System.out.println(sm.getSharePath());
        String str = "中文测试";
        sm.write(40, 20, str.getBytes("UTF-8"));
        byte[] b = new byte[20];
        byte[] b1 = new byte[20];
        sm.read(40, 20, b);
        sm1.read(40,20,b1);
        System.out.println(new String(b,"UTF-8"));
        System.out.println(new String(b1,"UTF-8"));
    }
}
