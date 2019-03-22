package com.syswin.temail.gateway.channels.clients.grpc;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.Data;

//just test connection and reset mechinisam

@Data
public class GrpcConcurrentTaskOnLine implements Runnable {

  private static final Random RANDOM = new Random(2);

  private final int cyclicTimes = 5;

  private final GrpcConcurrentData grpcConcrData;

  public GrpcConcurrentTaskOnLine(GrpcConcurrentData grpcConcrData) {
    this.grpcConcrData = grpcConcrData;
  }

  @Override
  public void run() {
    //start registry and heartBeat
    //sleep leave us time to check reconnect log
    grpcConcrData.init4Test();
    grpcConcrData.grpcClientWrapper.initClient();
    while (!Thread.currentThread().isInterrupted()) {
      try {
        for (int i = 0; i < grpcConcrData.temailAccoutLocations.size(); i++) {
          grpcConcrData.grpcClientWrapper.syncChannelLocations(grpcConcrData.temailAccoutLocations.get(i));
          TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(20));
          grpcConcrData.grpcClientWrapper.removeChannelLocations(grpcConcrData.temailAccoutLocations.get(i));
          TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(20));
        }
        TimeUnit.MILLISECONDS.sleep(100 + RANDOM.nextInt(100));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        e.printStackTrace();
      }
    }
  }
}
