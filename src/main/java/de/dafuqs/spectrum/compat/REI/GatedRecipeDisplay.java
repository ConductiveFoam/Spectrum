package de.dafuqs.spectrum.compat.REI;

import me.shedaniel.rei.api.common.display.*;

public interface GatedRecipeDisplay extends Display {
	
	boolean isUnlocked();
	
	boolean isSecret();
	
}
