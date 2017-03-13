package net.util.cache;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Properties;

/**
 * Created by hanlia on 2016/12/27.
 * 服务端缓存重发共享内存结构
 * java共享内存通过将磁盘文件区域映射到内存实现
 * 该区域数据结构包括（以下描述中磁盘文件区域与共享内存等价）：
 * 共享内存大小memorySize
 * 共享内存里实际存在的文件大小fsize
 * 共享文件名shareFileName
 * 共享内存路径sharePath
 * 共享内存缓存区mapBuf
 * 文件通道fchannel
 * 文件锁flock
 * 共享内存属性props
 * 随机存取文件对象RAFile
 */
public class ServerCachedMemoryDataStruct {
    private int memorySize = 1024*1024;                    //开辟共享内存大小
    private int fsize = 0;                          //文件的实际大小
    private String shareFileName;                   //共享内存文件名
    private String sharePath;                       //共享内存路径
    private MappedByteBuffer mapBuf = null;         //定义共享内存缓冲区
    private FileChannel fchannel = null;                  //定义相应的文件通道
    private FileLock flock = null;                     //定义文件区域锁定的标记。
    private Properties props = null;
    private RandomAccessFile RAFile = null;         //定义一个随机存取文件对象
    /**
     * 构造器
     */
    public ServerCachedMemoryDataStruct(String shareFileName, String sharePath) {
        if (sharePath.length()==0 || sharePath.equals("default")) {
            this.sharePath = System.getProperty("user.dir") + File.separator;
        } else { this.sharePath = sharePath;}
        if(shareFileName.length()==0 || shareFileName.equals("default")){
            this.shareFileName="serverCache";
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
    }
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
        ServerCachedMemoryDataStruct sm = new ServerCachedMemoryDataStruct("","");
        System.out.println(sm.getSharePath());
        String str = "中文测试";
        sm.write(40, 20, str.getBytes("UTF-8"));
        byte[] b = new byte[20];
        sm.read(40, 20, b);
        System.out.println(new String(b,"UTF-8"));
    }
}
