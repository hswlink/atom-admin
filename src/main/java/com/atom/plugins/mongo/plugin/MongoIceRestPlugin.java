package com.atom.plugins.mongo.plugin;
//package com.cybermkd.mongo.plugin;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.cybermkd.mongo.kit.MongoKit;
//import com.mongodb.MongoClient;
//import com.sun.webkit.plugin.Plugin;
//
///**
// * 创建人:T-baby
// * 创建日期: 16/4/17
// * 文件描述:MongoDB for Resty 插件
// */
//
//public class MongoIceRestPlugin extends MongoPlugin implements Plugin {
//
//
//    final Logger logger = LoggerFactory.getLogger(MongoIceRestPlugin.class);
//
//    private MongoClient client;
//
//    @Override
//    public boolean start() {
//        client = getMongoClient();
//        MongoKit.INSTANCE.init(client, getDatabase());
//        return true;
//    }
//
//    @Override
//    public boolean stop() {
//        if (client != null) {
//            client.close();
//        }
//        return true;
//    }
//
//
//}
