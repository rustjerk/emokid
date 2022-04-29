package ru.sigsegv.lab7.common.model;

import ru.sigsegv.lab7.client.CommandDeserializer;
import ru.sigsegv.lab7.common.serde.*;
import ru.sigsegv.lab7.common.util.IdGenerator;
import ru.sigsegv.lab7.common.util.Nullable;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Music band data record
 */
public class MusicBand implements Serializable, Deserializable, Comparable<MusicBand> {
    @SkipDeserialization(deserializer = CommandDeserializer.class)
    private long id = IdGenerator.generateId();

    private String name = "Dora";
    private Coordinates coordinates = new Coordinates();

    @SkipDeserialization(deserializer = CommandDeserializer.class)
    private ZonedDateTime creationDate = ZonedDateTime.now();
    private Integer numberOfParticipants = 1;
    private String description = "Unknown";

    @Nullable
    private MusicGenre genre;

    @Nullable
    private Studio studio;

    /**
     * @param id band identifier (default id is generated to be unique)
     */
    public void setId(long id) {
        if (id <= 0)
            throw new IllegalArgumentException("music band id cannot be null");
        this.id = id;
    }

    /**
     * @return band identifier
     */
    public long getId() {
        return id;
    }

    /**
     * @param name band name
     * @throws IllegalArgumentException if name is null or empty
     */
    public void setName(String name) {
        if (name == null)
            throw new IllegalArgumentException("music band name cannot be null");
        if (name.isEmpty())
            throw new IllegalArgumentException("music band name cannot be empty");
        this.name = name;
    }

    /**
     * @return band name
     */
    public String getName() {
        return name;
    }

    /**
     * @param coordinates band coordinates
     * @throws IllegalArgumentException if coordinates is null
     */
    public void setCoordinates(Coordinates coordinates) {
        if (coordinates == null)
            throw new IllegalArgumentException("music band coordinates cannot be null");
        this.coordinates = coordinates;
    }

    /**
     * @return band coordinates
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * @param creationDate band creation date
     * @throws IllegalArgumentException if creationDate is null
     */
    public void setCreationDate(ZonedDateTime creationDate) {
        if (coordinates == null)
            throw new IllegalArgumentException("music band creation date cannot be null");
        this.creationDate = creationDate;
    }

    /**
     * @return band creation date
     */
    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * @param numberOfParticipants number of band's participants
     * @throws IllegalArgumentException if numberOfParticipants is less or equal to zero
     */
    public void setNumberOfParticipants(int numberOfParticipants) {
        if (numberOfParticipants <= 0)
            throw new IllegalArgumentException("music band number of participants should be nonzero");
        this.numberOfParticipants = numberOfParticipants;
    }

    /**
     * @return number of band's participants
     */
    public int getNumberOfParticipants() {
        return numberOfParticipants;
    }

    /**
     * @param description band description
     * @throws IllegalArgumentException if description is null
     */
    public void setDescription(String description) {
        if (description == null || description.isEmpty())
            throw new IllegalArgumentException("music band description cannot be null");
        this.description = description;
    }

    /**
     * @return band description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param genre band genre (nullable)
     */
    public void setGenre(MusicGenre genre) {
        this.genre = genre;
    }

    /**
     * @return band genre (nullable)
     */
    public MusicGenre getGenre() {
        return genre;
    }

    /**
     * @param studio band studio
     */
    public void setStudio(Studio studio) {
        this.studio = studio;
    }

    /**
     * @return band studio
     */
    public Studio getStudio() {
        return studio;
    }

    @Override
    public String toString() {
        return "MusicBand{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", numberOfParticipants=" + numberOfParticipants +
                ", description='" + description + '\'' +
                ", genre=" + genre +
                ", studio=" + studio +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicBand musicBand = (MusicBand) o;
        return id == musicBand.id
                && name.equals(musicBand.name)
                && coordinates.equals(musicBand.coordinates)
                && creationDate.equals(musicBand.creationDate)
                && numberOfParticipants.equals(musicBand.numberOfParticipants)
                && description.equals(musicBand.description)
                && genre == musicBand.genre
                && Objects.equals(studio, musicBand.studio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, coordinates, creationDate, numberOfParticipants, description, genre, studio);
    }

    private static final StructSerializer<MusicBand> SERIALIZER = new StructSerializer<>(MusicBand.class);
    private static final StructDeserializer<MusicBand> DESERIALIZER = new StructDeserializer<>(MusicBand.class);

    @Override
    public void serialize(Serializer serializer) {
        SERIALIZER.serialize(serializer, this);
    }

    @Override
    public void deserialize(Deserializer deserializer) throws DeserializeException {
        DESERIALIZER.deserialize(deserializer, this);
    }

    @Override
    public int compareTo(MusicBand o) {
        return name.compareTo(o.name);
    }
}
