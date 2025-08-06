package com.xray;

import com.xray.app.proxyman.command.*;
import com.xray.app.stats.command.*;
import com.xray.common.protocol.User;
import com.xray.common.serial.TypedMessage;
import com.xray.proxy.shadowsocks.Account;
import com.xray.proxy.shadowsocks.CipherType;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {

    private final String api = "103.21.91.50"; //添加用户的地址
    private final int port = 12345; //添加用户的端口
    private final String serverPassword = "wCs2znu6mduAjt5XsQWiBqax"; //添加grpc鉴权的密码

    public static void main(String[] args) {
        try {
            Main m = new Main();
            m.add("shadowsocks", "linbeilinbei2343555", "12345671", 0, CipherType.CHACHA20_POLY1305);
//            m.delete("shadowsocks", "test@example.com");
            //   System.out.println(m.getStat("linbeilinbei", "uplink"));
            Scanner sc = new Scanner(System.in);
            System.out.print("Please enter a string : ");

            System.out.print("Your input is :" + sc.next());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 添加用户
     *
     * @param tag      inbound Tag
     * @param email    用户邮箱   //用户邮箱,我自己定义
     * @param password 用户密码   //用户密码,我自己定义
     * @param level    用户等级    //目前默认传0
     * @param cipher   加密cipher //cipher  默认传: CHACHA20_POLY1305
     * @return AlterInboundResponse
     */
    private AlterInboundResponse add(String tag, String email, String password, Integer level, CipherType cipher) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(api, port).usePlaintext().build();
        HandlerServiceGrpc.HandlerServiceBlockingStub stub = HandlerServiceGrpc.newBlockingStub(channel);

        Account account = Account.newBuilder().setPassword(password).setCipherType(cipher).build();
        TypedMessage accountMessage = TypedMessage.newBuilder().setType(account.getDescriptorForType().getFullName())
                .setValue(account.toByteString()).build();
        User user = User.newBuilder().setLevel(level).setEmail(email).setAccount(accountMessage).build();

        AddUserOperation operation = AddUserOperation.newBuilder().setUser(user).build();
        TypedMessage operationMessage = TypedMessage.newBuilder().setType(operation.getDescriptorForType().getFullName())
                .setValue(operation.toByteString()).build();
        AlterInboundRequest req = AlterInboundRequest.newBuilder().setTag(tag).setPassword(serverPassword).setOperation(operationMessage).build();
        AlterInboundResponse resp = stub.alterInbound(req);
        channel.shutdown();
        return resp;

    }


    /**
     * 删除用户
     *
     * @param tag   inbound tag
     * @param email 用户邮箱
     * @return AlterInboundResponse
     */
    private AlterInboundResponse delete(String tag, String email) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(api, port).usePlaintext().build();
        HandlerServiceGrpc.HandlerServiceBlockingStub stub = HandlerServiceGrpc.newBlockingStub(channel);
        RemoveUserOperation operation = RemoveUserOperation.newBuilder().setEmail(email).build();
        TypedMessage operationMessage = TypedMessage.newBuilder().setType(operation.getDescriptorForType().getFullName())
                .setValue(operation.toByteString()).build();
        AlterInboundRequest req = AlterInboundRequest.newBuilder().setTag(tag).setPassword(serverPassword).setOperation(operationMessage).build();
        AlterInboundResponse resp = stub.alterInbound(req);
        channel.shutdown();
        return resp;
    }

    /**
     * 获取指定用户的流量
     *
     * @param name 用户名
     * @param type 类型 uplink downlink
     * @return long
     */
    private long getStat(String name, String type) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(api, port).usePlaintext().build();
        try {
            StatsServiceGrpc.StatsServiceBlockingStub stub = StatsServiceGrpc.newBlockingStub(channel);
            GetStatsRequest req = GetStatsRequest.newBuilder().setName("user>>>" + name + ">>>traffic>>>" + type).setReset(true).build();
            GetStatsResponse resp = stub.getStats(req);
            System.out.println(resp.getStat().getValue());
            return resp.getStat().getValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            channel.shutdown();
        }
        return 0;
    }

    /**
     * 查询指定pattern的流量
     *
     * @param pattern user>>>test@example.com>>>traffic
     * @return QueryStatsResponse
     */
    private QueryStatsResponse queryStat(String pattern) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(api, port).usePlaintext().build();
        StatsServiceGrpc.StatsServiceBlockingStub stub = StatsServiceGrpc.newBlockingStub(channel);
        QueryStatsRequest req = QueryStatsRequest.newBuilder().setPattern(pattern).build();
        QueryStatsResponse resp = stub.queryStats(req);
        for (int i = 0; i < resp.getStatCount(); i++) {
            System.out.println(resp.getStat(i).getName());
            System.out.println(resp.getStat(i).getValue());
        }
        channel.shutdown();
        return resp;
    }
}
