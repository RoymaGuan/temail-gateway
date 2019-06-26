/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.gateway.notify;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.io.IOException;

public class PacketDeserializer extends TypeAdapter<CDTPPacket> {

  private final Gson gson = new Gson();
  private final TypeAdapter<CDTPPacket> packetAdapter = gson.getAdapter(CDTPPacket.class);
  private final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

  @Override
  public void write(JsonWriter out, CDTPPacket value) throws IOException {
    packetAdapter.write(out, value);
  }

  @Override
  public CDTPPacket read(JsonReader in) throws IOException {
    JsonElement tree = elementAdapter.read(in);
    JsonElement dataElement = tree.getAsJsonObject().get("data");
    tree.getAsJsonObject().remove("data");

    CDTPPacket packet = gson.fromJson(tree, CDTPPacket.class);
    if (dataElement != null) {
      packet.setData(dataElement.getAsString().getBytes());
    }

    return packet;
  }
}
