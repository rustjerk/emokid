package ru.sigsegv.lab7.common.model;

import ru.sigsegv.lab7.common.serde.*;

import java.util.Objects;

/**
 * 2D euclidean coordinates pair
 */
public class Coordinates implements Serializable, Deserializable {
    private Double x = 0.0;
    private long y = 0;

    /**
     * @param x abscissa
     */
    public void setX(double x) {
        if (x > 41)
            throw new IllegalArgumentException("x cannot be above 41");
        this.x = x;
    }

    /**
     * @return the abscissa
     */
    public double getX() {
        return x;
    }

    /**
     * @param y ordinate
     */
    public void setY(long y) {
        if (y > 107)
            throw new IllegalArgumentException("y cannot be above 107");
        this.y = y;
    }

    /**
     * @return the ordinate
     */
    public long getY() {
        return y;
    }

    private static final StructSerializer<Coordinates> SERIALIZER = new StructSerializer<>(Coordinates.class);
    private static final StructDeserializer<Coordinates> DESERIALIZER = new StructDeserializer<>(Coordinates.class);

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
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return y == that.y && x.equals(that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
