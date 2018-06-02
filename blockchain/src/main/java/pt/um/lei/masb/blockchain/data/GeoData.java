package pt.um.lei.masb.blockchain.data;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * Data constrained to some latitude and longitude coordinates.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class GeoData {
    @Id
    @GeneratedValue
    private long id;


    @Basic(optional = false)
    private final BigDecimal lat;

    @Basic(optional = false)
    private final BigDecimal lng;


    protected GeoData() {
        lat = null;
        lng = null;
    }

    GeoData(@DecimalMin(value = "-90")
            @DecimalMax(value = "90")
                    BigDecimal lat,
            @DecimalMin(value = "-180")
            @DecimalMax(value = "180")
                    BigDecimal lng) {
        this.lat = lat;
        this.lng = lng;
    }

    /**
     * @return Latitude for this sensor reading.
     */
    public BigDecimal getLat() {
        return lat;
    }

    /**
     * @return Longitude for this sensor reading.
     */
    public BigDecimal getLng() {
        return lng;
    }
}