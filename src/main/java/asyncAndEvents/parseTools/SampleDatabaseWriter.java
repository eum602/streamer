package asyncAndEvents.parseTools;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;

public class SampleDatabaseWriter {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    AsyncFile file = vertx.fileSystem().openBlocking("sample.db",
      new OpenOptions().setWrite(true).setCreate(true));

    Buffer buffer = Buffer.buffer();

    // Magic number
    buffer.appendBytes(new byte[] { 1, 2, 3, 4});

    // Version
    buffer.appendInt(2);

    // DB name
    buffer.appendString("Sample database\n");

    // Entry 1
    String key = "abc";
    String value = "123456-abcdef";
    buffer
      .appendInt(key.length())
      .appendString(key)
      .appendInt(value.length())
      .appendString(value);

    // Entry 2
    key = "foo@bar";
    value = "Foo Bar Baz";
    buffer
      .appendInt(key.length())
      .appendString(key)
      .appendInt(value.length())
      .appendString(value);

    file.end(buffer, ar -> vertx.close());

    /*we had little data we used a single buffer that we prepared wholly before writing it to the file, but we
    could equally use a buffer for the header, and new buffers for each key / value entry.*/
  }
}
