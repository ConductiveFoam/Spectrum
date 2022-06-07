package de.dafuqs.spectrum.compat.REI;

import de.dafuqs.spectrum.recipe.potion_workshop.PotionWorkshopBrewingRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.registry.RecipeManagerContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class PotionWorkshopBrewingRecipeDisplay extends PotionWorkshopRecipeDisplay {
	
	protected final StatusEffect statusEffect;
	
	/**
	 * When using the REI recipe functionality
	 *
	 * @param recipe The recipe
	 */
	public PotionWorkshopBrewingRecipeDisplay(PotionWorkshopBrewingRecipe recipe) {
		super(recipe);
		this.statusEffect = recipe.getStatusEffect();
	}
	
	/**
	 * When using Shift click on the plus button in the REI gui to autofill crafting grids
	 */
	public PotionWorkshopBrewingRecipeDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, PotionWorkshopBrewingRecipe recipe) {
		super(inputs, outputs, recipe);
		this.statusEffect = recipe.getStatusEffect();
	}
	
	public static Serializer<PotionWorkshopRecipeDisplay> serializer() {
		return Serializer.ofSimple(PotionWorkshopBrewingRecipeDisplay::simple).inputProvider(PotionWorkshopRecipeDisplay::getOrganisedInputEntries);
	}
	
	private static @NotNull PotionWorkshopRecipeDisplay simple(List<EntryIngredient> inputs, List<EntryIngredient> outputs, @NotNull Optional<Identifier> identifier) {
		Recipe<?> optionalRecipe = identifier.flatMap(resourceLocation -> RecipeManagerContext.getInstance().getRecipeManager().get(resourceLocation)).orElse(null);
		return new PotionWorkshopBrewingRecipeDisplay(inputs, outputs, (PotionWorkshopBrewingRecipe) optionalRecipe);
	}
	
	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return SpectrumPlugins.POTION_WORKSHOP_BREWING;
	}
	
}