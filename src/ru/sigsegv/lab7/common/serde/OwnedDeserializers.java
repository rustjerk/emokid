package ru.sigsegv.lab7.common.serde;

import ru.sigsegv.lab7.common.model.MusicGenre;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

/**
 * Set of all registered owned deserializers
 */
public class OwnedDeserializers {
    private final static HashMap<Class<?>, OwnedDeserializer<?>> deserializers = new HashMap<>();

    /**
     * Get a deserializer
     *
     * @param clazz Deserializable class
     * @param <T>   Type of the class
     * @return owned deserializer, or null if it doesn't exist
     */
    public static <T> OwnedDeserializer<T> get(Class<T> clazz) {
        return (OwnedDeserializer<T>) deserializers.get(clazz);
    }

    /**
     * Register a deserializer
     *
     * @param clazz        deserializable class
     * @param deserializer deserializer
     * @param <T>          Type of the class
     */
    public static <T> void register(Class<T> clazz, OwnedDeserializer<T> deserializer) {
        deserializers.put(clazz, deserializer);
    }

    static {
        register(boolean.class, Deserializer::deserializeBoolean);
        register(Boolean.class, Deserializer::deserializeBoolean);

        register(byte.class, s -> (byte) s.deserializeLong());
        register(Byte.class, s -> (byte) s.deserializeLong());

        register(short.class, s -> (short) s.deserializeLong());
        register(Short.class, s -> (short) s.deserializeLong());

        register(int.class, s -> (int) s.deserializeLong());
        register(Integer.class, s -> (int) s.deserializeLong());

        register(long.class, Deserializer::deserializeLong);
        register(Long.class, Deserializer::deserializeLong);

        register(float.class, s -> (float) s.deserializeDouble());
        register(Float.class, s -> (float) s.deserializeDouble());

        register(double.class, Deserializer::deserializeDouble);
        register(Double.class, Deserializer::deserializeDouble);

        register(String.class, Deserializer::deserializeString);

        register(ZonedDateTime.class, d -> {
            try {
                d.setHelp("example: 2007-12-03T10:15:30+01:00[Europe/Paris]");
                return ZonedDateTime.parse(d.deserializeString(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new DeserializeException(d.formatErrorMessage(e.getMessage()));
            }
        });

        register(MusicGenre.class, d -> {
            String variants = "variants: ";
            for (MusicGenre genre : MusicGenre.values())
                variants += genre.toString() + ", ";
            d.setHelp(variants.substring(0, variants.length() - 2));
            MusicGenre genre = MusicGenre.fromString(d.deserializeString());
            if (genre == null) throw new DeserializeException(d.formatErrorMessage("no such music genre"));
            return genre;
        });
    }
}
