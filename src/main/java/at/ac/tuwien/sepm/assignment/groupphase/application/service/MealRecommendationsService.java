package at.ac.tuwien.sepm.assignment.groupphase.application.service;

import at.ac.tuwien.sepm.assignment.groupphase.application.dto.Recipe;
import at.ac.tuwien.sepm.assignment.groupphase.application.dto.RecipeTag;
import at.ac.tuwien.sepm.assignment.groupphase.application.persistence.NoEntryFoundException;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface MealRecommendationsService {

    /**
     * Calculates recipes for the day that best correspond with the current active plan.
     * @return A list of the recommended meals for the day in accordance with a plan
     */
    public Map<RecipeTag, Recipe> getRecommendedMeals() throws ServiceInvokationException, NoEntryFoundException, NoOptimalSolutionException;

    /**
     * Calculates a meal recommendation for a specific meal
     * @param meal {@link RecipeTag} Meal for which to calculate
     * @param omissions List of {@link Recipe} to omit when selecting a recommendation
     * @return Recommended {@link Recipe}
     * @throws ServiceInvokationException
     * @throws NoEntryFoundException
     * @throws NoOptimalSolutionException
     */
    public Recipe getRecommendedMeal(RecipeTag meal, List<Recipe> omissions) throws ServiceInvokationException, NoOptimalSolutionException;
}
