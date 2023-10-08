package cn.itcast.hotel.constant;

public class MQConstant {
    /**
     * 交换机
     */
    public static final String HOTEL_EXCHANGE = "hotel.topic";
    /**
     * 新增修改队列
     */
    public static final String HOTEL_INSERT_QUEUE = "hotel.insert.queue";
    /**
     * 删除队列
     */
    public static final String HOTEL_DELETE_QUEUE = "hotel.delete.queue";
    /**
     * 新增修改key
     */
    public static final String HOTEL_INSERT_KEY = "hotel.insert";
    /**
     * 删除key
     */
    public static final String HOTEL_DELETE_KEY = "hotel.delete";
}
