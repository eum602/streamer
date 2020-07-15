package asyncAndEvents.parseTools;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.RecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseReader {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseReader.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    AsyncFile file = vertx.fileSystem().openBlocking("sample.db",
      new OpenOptions().setRead(true));

    RecordParser parser = RecordParser.newFixed(4, file); //We first want to read the "magic number", so reading the first 4 bytes (check te SampleDatabaseWriter)
    parser.handler(header -> readMagicNumber(header, parser));//sending to read the magic number; and also ALWAYS PASSING THE PARSER TO THE HANDLER, so other methods can continue decoding it
    parser.endHandler(v -> vertx.close());
  }
  /*
  * readMagicNumber method extracts the 4 bytes of the magic number from a buffer.
  * */
  private static void readMagicNumber(Buffer header, RecordParser parser) {
    logger.info("Magic number: {}:{}:{}:{}", header.getByte(0), header.getByte(1), header.getByte(2), header.getByte(3));/*printing the magic number,
    */
    parser.handler(version -> readVersion(version, parser));
  }

  /*
  * we don’t have to change the parser mode since an integer is 4 bytes
  * */
  private static void readVersion(Buffer header, RecordParser parser) {
    logger.info("Version: {}", header.getInt(0));//and getting the next version which is 1 byte long
    parser.delimitedMode("\n");//parser mode limited on the fly; getting the "name" field in the buffer by cutting by "jump line"
    parser.handler(name -> readName(name, parser));
  }

  /*
  *
  * */
  private static void readName(Buffer name, RecordParser parser) {
    logger.info("Name: {}", name.toString());//printing the name
    parser.fixedSizeMode(4); //now getting the next four bytes (corresponding to the key length
    parser.handler(keyLength -> readKey(keyLength, parser)); //parsing with the readKey method
  }

  private static void readKey(Buffer keyLength, RecordParser parser) {
    parser.fixedSizeMode(keyLength.getInt(0));//now as we know the key length, using this to get the key
    parser.handler(key -> readValue(key.toString(), parser)); //now the parse.handler returns the key and we read it with readValue method
  }

  private static void readValue(String key, RecordParser parser) {
    //logger.info("Key: {}", key.toString());//printing the key ==> commenting this because I am going to print this key together witn the value soon.
    parser.fixedSizeMode(4);//Now getting the lenght of the "value" (recall we are reading a key, value in a database)
    parser.handler(valueLength -> finishEntry(key, valueLength, parser));
  }

  private static void finishEntry(String key, Buffer valueLength, RecordParser parser) {
    parser.fixedSizeMode(valueLength.getInt(0));//getting the value with the help of the length of the value
    parser.handler(value -> {//parser returns the value
      logger.info("Key: {} ; Value: {}", key, value);
      //////This process repeats again from here because we are again reading the first 4 bytes corresponding to the key lenght of the next entry in the DB.
      parser.fixedSizeMode(4);
      parser.handler(keyLength -> readKey(keyLength, parser));//then sending to the readKeyHandler and so on and so on until finishing reading the whole DB.
    });
  }
}
