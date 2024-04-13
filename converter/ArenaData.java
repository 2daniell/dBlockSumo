package com.daniel.blocksumo.converter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.World;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArenaData {

    private int startX, startY, startZ;
    private int endX, endY, endZ;
}
