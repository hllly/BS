package service.sreg;

/**
 * Created by hanlia on 2017/1/12.
 */
public class TestService extends BaseService{
    public int getRemoteInt(Integer localInt){
        return localInt*2;
    }
    public Double getRemoteDouble(Double localDouble){ return localDouble*2.0; }
}
