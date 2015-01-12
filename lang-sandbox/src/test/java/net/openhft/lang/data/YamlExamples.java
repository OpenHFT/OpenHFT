package net.openhft.lang.data;

import java.io.StreamCorruptedException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by peter on 12/01/15.
 */
public class YamlExamples {
    enum Keys implements WireKey {
        list(List.class, Collections.emptyList()),
        american(List.class, Collections.emptyList()),
        national(List.class, Collections.emptyList()),
        name(""),
        time(LocalTime.MIN),
        player(""),
        action(""),
        hr(0),
        avg(0.0),
        rbi(0L),
        canonical(ZonedDateTime.of(0, 0, 0, 0, 0, 0, 0, ZoneId.systemDefault())),
        date(LocalDate.MIN);

        static {
            WireKey.checkKeys(values());
        }

        private final Type type;
        private final Object defaultValue;

        Keys(Object defaultValue) {
            this(defaultValue.getClass(), defaultValue);
        }

        Keys(Type type, Object defaultValue) {
            this.type = type;
            this.defaultValue = defaultValue;
        }

        @Override
        public Type type() {
            return type;
        }

        @Override
        public Object defaultValue() {
            return defaultValue;
        }
    }

    public static void sequenceExample(Wire wire) {
        /*
         - Mark McGwire
         - Sammy Sosa
         - Ken Griffey
         */
        wire.writeSequenceStart(Keys.list);
        for (String s : "Mark McGwire,Sammy Sosa,Ken Griffey".split(","))
            wire.writeText(null, s);
        wire.writeSequenceEnd();
        // or
        wire.writeSequenceLength(Keys.list, 3);
        for (String s : "Mark McGwire,Sammy Sosa,Ken Griffey".split(","))
            wire.writeText(null, s);

        // or
        wire.writeSequence(Keys.list, "Mark McGwire,Sammy Sosa,Ken Griffey".split(","));
        // or
        wire.writeSequence(Keys.list, Arrays.asList("Mark McGwire,Sammy Sosa,Ken Griffey".split(",")));

        // to read this.
        wire.readSequenceStart(Keys.list);
        List<String> strings = new ArrayList<String>();
        while (wire.hasNextSequenceItem())
            strings.add(wire.readText());
        wire.readSequenceEnd();

        // or
        int len = wire.readSequenceLength(Keys.list);
        String[] strings2 = new String[len];
        for (int i = 0; i < len; i++)
            strings2[i] = wire.readText();

        // or
        wire.readSequence(Keys.list, strings, String.class);
    }

    public static void mapExample(Wire wire) {
/*
        hr:  65    # Home runs
        avg: 0.278 # Batting average
        rbi: 147   # Runs Batted In
*/
        wire.writeMappingStart();
        wire.writeInt(Keys.hr, 65);
        wire.writeComment("Home runs");
        wire.writeDouble(Keys.avg, 0.278);
        wire.writeComment("Batting average");
        wire.writeLong(Keys.rbi, 147);
        wire.writeComment("Runs Batted In");
        wire.writeMappingEnd();

        wire.flip();
        wire.readMappingStart();
        int hr = wire.readInt(Keys.hr);
        wire.readComment(); // optional
        double avg = wire.readDouble(Keys.avg);
        wire.readComment(); // optional
        long rbi = wire.readLong(Keys.rbi);
        wire.readComment(); // optional
        wire.readMappingEnd();
        wire.clear();
        
        /*
        american:
          - Boston Red Sox
          - Detroit Tigers
          - New York Yankees
        national:
          - New York Mets
          - Chicago Cubs
          - Atlanta Braves
         */
        wire.writeSequence(Keys.american, "Boston Red Sox", "Detroit Tigers", "New York Yankees");
        wire.writeSequence(Keys.national, "New York Mets", "Chicago Cubs", "Atlanta Braves");

        wire.readSequenceStart(Keys.american);
        while (wire.hasNextSequenceItem())
            wire.readText();
        wire.readSequenceEnd();

        List<String> team = new ArrayList<String>();
        wire.readSequence(Keys.national, team, String.class);
        
        /*
        -
          name: Mark McGwire
          hr:   65
          avg:  0.278
        -
          name: Sammy Sosa
          hr:   63
          avg:  0.288
         */
        wire.writeSequenceStart();
        wire.writeMappingStart();
        wire.writeText(Keys.name, "Mark McGwire");
        wire.writeInt(Keys.hr, 65);
        wire.writeDouble(Keys.avg, 0.278);
        wire.writeMappingEnd();

        wire.writeMappingStart();
        wire.writeText(Keys.name, "Sammy Sosa");
        wire.writeInt(Keys.hr, 63);
        wire.writeDouble(Keys.avg, 0.288);
        wire.writeMappingEnd();
        wire.writeSequenceEnd();

        wire.flip();

        wire.readSequenceStart();
        while (wire.hasNextSequenceItem()) {
            wire.readMappingStart();
            String name = wire.readText(Keys.name);
            int hr2 = wire.readInt(Keys.hr);
            double avg2 = wire.readDouble(Keys.avg);
            wire.readMappingEnd();
        }
        wire.readSequenceEnd();

        wire.clear();
        /*
        Mark McGwire: {hr: 65, avg: 0.278}
        Sammy Sosa: {
            hr: 63,
            avg: 0.288
          }
         */
        wire.writeMappingStart("Mark McGwire", Keys.name);
        wire.writeInt(Keys.hr, 65);
        wire.writeDouble(Keys.avg, 0.278);
        wire.writeMappingEnd();

        wire.writeMappingStart("Sammy Sosa", Keys.name);
        wire.writeInt(Keys.hr, 63);
        wire.writeDouble(Keys.avg, 0.288);
        wire.writeMappingEnd();

        wire.flip();

        StringBuilder name = new StringBuilder();
        while (wire.hasMapping()) {
            wire.readMappingStart(name, Keys.name);
            int hr2 = wire.readInt(Keys.hr);
            double avg2 = wire.readDouble(Keys.avg);
            wire.readMappingEnd();
        }
        wire.clear();

        /*
        ---
        time: 20:03:20
        player: Sammy Sosa
        action: strike (miss)
        ...
        ---
        time: 20:03:47
        player: Sammy Sosa
        action: grand slam
        ...
        */
        wire.writeDocumentStart();
        wire.writeTime(Keys.time, LocalTime.of(20, 3, 20));
        wire.writeText(Keys.player, "Sammy Sosa");
        wire.writeText(Keys.action, "strike (miss)");
        wire.writeDocumentEnd();
        wire.writeDocumentStart();
        wire.writeTime(Keys.time, LocalTime.of(20, 3, 47));
        wire.writeText(Keys.player, "Sammy Sosa");
        wire.writeText(Keys.action, "grand slam");
        wire.writeDocumentEnd();

        wire.flip();
        while (wire.hasDocument()) {
            wire.readDocumentStart();
            LocalTime time = wire.readTime(Keys.time);
            String player = wire.readText(Keys.player);
            String action = wire.readText(Keys.action);
            wire.readDocumentEnd();
        }
        wire.clear();

        /*
        canonical: 2001-12-15T02:59:43.1Z
        iso8601: 2001-12-14t21:59:43.10-05:00
        spaced: 2001-12-14 21:59:43.10 -5
        date: 2002-12-14
        */

        wire.writeZonedDateTime(Keys.canonical, ZonedDateTime.parse("2001-12-15T02:59:43.1Z"));
        ZonedDateTime zdt = wire.readZonedDateTime(Keys.canonical);

        wire.writeDate(Keys.date, LocalDate.of(2002, 12, 14));
        LocalDate ld = wire.readDate();
    }

    public static void object(Wire wire) {
        /*
        !myType {
            name: Hello World
            date: 2015-01-12
         }
         */
        MyType myType = new MyType();
        wire.writeMarshallable(myType);
        wire.flip();
        wire.readMarshallable(myType);
    }
}

class MyType implements Marshallable {
    String name;
    LocalDate date;

    @Override
    public void writeMarshallable(Wire wire) {
        wire.writeText(MyTypeKeys.name, name);
        wire.writeDate(MyTypeKeys.date, date);
    }

    @Override
    public void readMarshallable(Wire wire) throws StreamCorruptedException {
        name = wire.readText(MyTypeKeys.name);
        date = wire.readDate(MyTypeKeys.date);
    }

    enum MyTypeKeys implements WireKey {
        name(""), date(LocalDate.MIN);

        private final Object defaultValue;

        static {
            WireKey.checkKeys(values());
        }

        MyTypeKeys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object defaultValue() {
            return defaultValue;
        }
    }
}

