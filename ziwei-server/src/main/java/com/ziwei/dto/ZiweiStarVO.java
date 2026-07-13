package com.ziwei.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Star view object (embedded in PalaceVO)
 *
 * @author JTWORLD
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZiweiStarVO {

    /** Star code (English identifier) */
    private String starCode;

    /** Star name in Chinese */
    private String starName;

    /** Brightness level name (庙/旺/得/利/平/陷/不) */
    private String brightness;

    /** Sihua type short name (禄/权/科/忌), null if not transformed */
    private String sihuaType;

}
