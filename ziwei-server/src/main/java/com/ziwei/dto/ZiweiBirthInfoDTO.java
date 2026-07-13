package com.ziwei.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Birth info DTO for chart calculation request
 *
 * @author JTWORLD
 */
@Data
public class ZiweiBirthInfoDTO {

    /** Solar/Gregorian year */
    @NotNull
    @Min(1900)
    @Max(2100)
    private int solarYear;

    /** Solar month (1-12) */
    @NotNull
    @Min(1)
    @Max(12)
    private int solarMonth;

    /** Solar day (1-31) */
    @NotNull
    @Min(1)
    @Max(31)
    private int solarDay;

    /** Hour in DiZhi index (0=子, 1=丑, ..., 11=亥) */
    @NotNull
    @Min(0)
    @Max(11)
    private int hour;

    /** Minute (0-59) */
    private int minute;

    /** Gender: 0=female, 1=male */
    @NotNull
    @Min(0)
    @Max(1)
    private int gender;

    /** Birth place */
    private String birthPlace;

    /** Whether daylight saving time applies */
    private Boolean isDst;

    /** User ID (temporary, will be replaced by auth) */
    private Long userId;

}
