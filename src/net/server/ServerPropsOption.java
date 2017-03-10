package net.server;

/**
 * Created by hanlia on 2016/12/22.
 * Server端参数限制值，包括某些最大值和最小值
 * 当用户指定的参数不在此范围内则当做默认参数处理
 */
public class ServerPropsOption {
    public static final int MAX_BOSSGROUPNUM=100;
    public static final int MAX_WORKERGROUPNUM=10000;
}
