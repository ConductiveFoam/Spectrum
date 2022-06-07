package de.dafuqs.spectrum.compat.REI;

import de.dafuqs.spectrum.recipe.anvil_crushing.AnvilCrushingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.Collections;
import java.util.List;

public class AnvilCrushingRecipeDisplay implements Display {
	
	public final float experience;
	public final float crushedItemsPerPointOfDamage;
	private final List<EntryIngredient> inputs;
	private final EntryIngredient output;
	
	public AnvilCrushingRecipeDisplay(AnvilCrushingRecipe recipe) {
		this.inputs = recipe.getIngredients().stream().map(EntryIngredients::ofIngredient).toList();
		this.output = EntryIngredients.of(recipe.getOutput());
		
		this.experience = recipe.getExperience();
		this.crushedItemsPerPointOfDamage = recipe.getCrushedItemsPerPointOfDamage();
	}
	
	@Override
	public List<EntryIngredient> getInputEntries() {
		return inputs;
	}
	
	@Override
	public List<EntryIngredient> getOutputEntries() {
		return Collections.singletonList(output);
	}
	
	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return SpectrumPlugins.ANVIL_CRUSHING;
	}
	
}