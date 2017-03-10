package service.remote;

import java.rmi.RemoteException;
/**
 * Created by hanlia on 2017/1/21.
 */
public interface Fib {
    public int getFib(int n) throws RemoteException;
}
