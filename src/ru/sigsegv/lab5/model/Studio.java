package ru.sigsegv.lab5.model;

import ru.sigsegv.lab5.serde.*;
import ru.sigsegv.lab5.util.Nullable;

import java.util.Objects;

/**
 * Studio data record
 */
public class Studio implements Serializable, Deserializable, Comparable<Studio> {
    @Nullable
    private String name = null;
    private String address = "";

    /**
     * @param name studio name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return studio name
     */
    public String getName() {
        return name;
    }

    /**
     * @param address studio address
     * @throws IllegalArgumentException if the address is null
     */
    public void setAddress(String address) {
        if (address == null || address.isEmpty())
            throw new IllegalArgumentException("studio name cannot be null");
        this.address = address;
    }

    /**
     * @return studio address
     */
    public String getAddress() {
        return address;
    }

    private static final StructSerializer<Studio> SERIALIZER = new StructSerializer<>(Studio.class);
    private static final StructDeserializer<Studio> DESERIALIZER = new StructDeserializer<>(Studio.class);

    @Override
    public void serialize(Serializer serializer) {
        SERIALIZER.serialize(serializer, this);
    }

    @Override
    public void deserialize(Deserializer deserializer) throws DeserializeException {
        DESERIALIZER.deserialize(deserializer, this);
    }

    @Override
    public String toString() {
        return "Studio{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Studio studio = (Studio) o;
        return Objects.equals(name, studio.name) && address.equals(studio.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @Override
    public int compareTo(Studio o) {
        if (name == null || o.name == null) return 0;
        return name.toLowerCase().compareTo(o.name.toLowerCase());
    }
}
