package at.ac.tuwien.sepm.assignment.groupphase.application.service.implementation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.sepm.assignment.groupphase.application.dto.IngredientSearchParam;
import at.ac.tuwien.sepm.assignment.groupphase.application.dto.Recipe;
import at.ac.tuwien.sepm.assignment.groupphase.application.dto.RecipeIngredient;
import at.ac.tuwien.sepm.assignment.groupphase.application.dto.RecipeTag;
import at.ac.tuwien.sepm.assignment.groupphase.application.persistence.PersistenceException;
import at.ac.tuwien.sepm.assignment.groupphase.application.persistence.RecipePersistence;
import at.ac.tuwien.sepm.assignment.groupphase.application.persistence.implementation.DBRecipePersistence;
import at.ac.tuwien.sepm.assignment.groupphase.application.service.RecipeService;
import at.ac.tuwien.sepm.assignment.groupphase.application.service.ServiceInvokationException;
import at.ac.tuwien.sepm.assignment.groupphase.application.util.BaseTest;

public class SimpleRecipeServiceTest extends BaseTest {

	// mocking
	private final RecipePersistence mockedRecipeRepo = mock(DBRecipePersistence.class);

	// example data
	private EnumSet<RecipeTag> validTagBreakfastSet = EnumSet.noneOf(RecipeTag.class);

	private static final String EXAMPLE_TEXT_256CHARS = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimatad";

	private List<RecipeIngredient> recipeIngredientList = new ArrayList<>();

	private Recipe recipeValid = new Recipe("My recipe", 120d, "Test", validTagBreakfastSet);
	private Recipe recipeInvalid1 = new Recipe(EXAMPLE_TEXT_256CHARS, 0d, "", EnumSet.noneOf(RecipeTag.class));
	private Recipe recipeInvalid2 = new Recipe("My recipe 2", 256d, "something to do", validTagBreakfastSet);

	public SimpleRecipeServiceTest() {
		validTagBreakfastSet.add(RecipeTag.B);

		// valid recipe ingredient list
		RecipeIngredient ri1 = new RecipeIngredient(2d, 55.5, 66.6, 77.7, 88.8, "oz", 120d, true, "Watermelon");
		RecipeIngredient ri2 = new RecipeIngredient(1.4, 88.9, 78d, 56d, 100d, "oz", 50d, true, "Cheese");
		recipeIngredientList.add(ri1);
		recipeIngredientList.add(ri2);

		RecipeIngredient ri3 = new RecipeIngredient(45, 3.5, false);
		RecipeIngredient ri4 = new RecipeIngredient(101, 2d, false);
		recipeIngredientList.add(ri3);
		recipeIngredientList.add(ri4);
		recipeValid.setRecipeIngredients(recipeIngredientList);

		// invalid recipe ingredient list
		recipeInvalid1.setRecipeIngredients(new ArrayList<>());
		recipeInvalid2.setRecipeIngredients(null);
	}

	@Test
	public void testCreate_validData_callsPersistenceCreateOnce()
			throws ServiceInvokationException, PersistenceException {

		// invokation
		RecipeService dietPlanService = new SimpleRecipeService(mockedRecipeRepo);
		dietPlanService.create(recipeValid);

		// verification after invokation
		verify(mockedRecipeRepo, times(1)).create(recipeValid);
	}

	@Test
	public void testCreate_invalidDataFallsBelowLimitsAndEmptyIngredientList_notCallsPersistenceCreateAndValidations()
			throws PersistenceException {
		// invokation
		RecipeService recipeService = new SimpleRecipeService(mockedRecipeRepo);
		try {
			recipeService.create(recipeInvalid1);
		} catch (ServiceInvokationException e) {

			// verification - no interaction with repo
			verifyZeroInteractions(mockedRecipeRepo);

			// verify validations
			ArrayList<String> errors = e.getContext().getErrors();
			Assert.assertEquals(5, errors.size());
			Assert.assertEquals("Enter only 255 characters in the field 'Recipe name'", errors.get(0));
			Assert.assertEquals("Enter a value that is greater than 0.0 in the field 'Duration'", errors.get(1));
			Assert.assertEquals("Enter at least 1 characters in the field 'Description'", errors.get(2));
			Assert.assertEquals("Select at least one tag (breakfast, lunch or dinner)", errors.get(3));
			Assert.assertEquals("Select at least one ingredient for the recipe.", errors.get(4));
			return;
		}
		Assert.fail("Should throw ServiceInvokationException!");
	}

	@Test
	public void testCreate_invalidDataDurationExceedsLimitAndIngredientListIsNull_notCallsPersistenceCreateAndValidations()
			throws PersistenceException {
		// invokation
		RecipeService recipeService = new SimpleRecipeService(mockedRecipeRepo);
		try {
			recipeService.create(recipeInvalid2);
		} catch (ServiceInvokationException e) {

			// verification - no interaction with repo
			verifyZeroInteractions(mockedRecipeRepo);

			// verify validations
			ArrayList<String> errors = e.getContext().getErrors();
			Assert.assertEquals(2, errors.size());
			Assert.assertEquals("Enter a value that is smaller than 255.0 in the field 'Duration'", errors.get(0));
			Assert.assertEquals("The field 'Ingredient Selection' cannot be null", errors.get(1));
			return;
		}
		Assert.fail("Should throw ServiceInvokationException!");
	}

	@Test
	public void testSearchIngredient_validData_callsPersistenceOnceAndReturnsListFromPersistence()
			throws ServiceInvokationException, PersistenceException {
		IngredientSearchParam searchParam = new IngredientSearchParam("eggnog");

		// prepare mock
		List<RecipeIngredient> mockedResult = new ArrayList<>();
		mockedResult.add(new RecipeIngredient(3, null, 5.55, 6.66, 7.77, 8.88, "cup", 10.10, false, "Eggnog"));

		// mock
		when(mockedRecipeRepo.searchIngredient(searchParam)).thenReturn(mockedResult);

		// invokation
		RecipeService dietPlanService = new SimpleRecipeService(mockedRecipeRepo);
		List<RecipeIngredient> actualResult = dietPlanService.searchIngredient(searchParam);

		// verification after invokation
		verify(mockedRecipeRepo, times(1)).searchIngredient(searchParam);

		Assert.assertEquals(mockedResult, actualResult);
	}

	@Test
	public void testSearchIngredient_validDataWherePersistenceReturnsEmptyList_callsPersistenceOnceAndReturnsEmptyListFromPersistence()
			throws ServiceInvokationException, PersistenceException {

		IngredientSearchParam searchParam = new IngredientSearchParam("jaja");

		// prepare mock - empty list
		List<RecipeIngredient> mockedResult = new ArrayList<>();

		// mock
		when(mockedRecipeRepo.searchIngredient(searchParam)).thenReturn(mockedResult);

		// invokation
		RecipeService dietPlanService = new SimpleRecipeService(mockedRecipeRepo);
		List<RecipeIngredient> actualResult = dietPlanService.searchIngredient(searchParam);

		// verification after invokation
		verify(mockedRecipeRepo, times(1)).searchIngredient(searchParam);

		Assert.assertEquals(mockedResult, actualResult);
	}

	@Test
	public void testSearchIngredient_invalidDataWhereParamObjectIsNull_notCallsPersistenceAndValidation() {

		IngredientSearchParam searchParam = null;

		// invokation
		RecipeService dietPlanService = new SimpleRecipeService(mockedRecipeRepo);
		try {
			dietPlanService.searchIngredient(searchParam);
		} catch (ServiceInvokationException e) {
			// verification - no interaction with repo
			verifyZeroInteractions(mockedRecipeRepo);
			Assert.assertEquals(1, e.getContext().getErrors().size());
			Assert.assertEquals("The field 'Ingredient Search Param' cannot be null",
					e.getContext().getErrors().get(0));
			return;
		}
		Assert.fail("Should throw ServiceInvokationException.");
	}
	
	@Test
	public void testSearchIngredient_invalidDataWhereIngredientNameIsNull_notCallsPersistenceAndValidation() {

		IngredientSearchParam searchParam = new IngredientSearchParam(null);

		// invokation
		RecipeService dietPlanService = new SimpleRecipeService(mockedRecipeRepo);
		try {
			dietPlanService.searchIngredient(searchParam);
		} catch (ServiceInvokationException e) {
			// verification - no interaction with repo
			verifyZeroInteractions(mockedRecipeRepo);
			Assert.assertEquals(1, e.getContext().getErrors().size());
			Assert.assertEquals("The field 'Ingredient Name' cannot be null",
					e.getContext().getErrors().get(0));
			return;
		}
		Assert.fail("Should throw ServiceInvokationException.");
	}
	
	@Test
	public void testSearchIngredient_invalidDataWhereIngredientNameLengthIsLessThan3Chars_notCallsPersistenceAndValidation() {

		IngredientSearchParam searchParam = new IngredientSearchParam(" Eg "); // 2 chars + 2 spaces

		// invokation
		RecipeService dietPlanService = new SimpleRecipeService(mockedRecipeRepo);
		try {
			dietPlanService.searchIngredient(searchParam);
		} catch (ServiceInvokationException e) {
			// verification - no interaction with repo
			verifyZeroInteractions(mockedRecipeRepo);
			Assert.assertEquals(1, e.getContext().getErrors().size());
			Assert.assertEquals("Enter at least 3 characters in the field 'Ingredient Name'",
					e.getContext().getErrors().get(0));
			return;
		}
		Assert.fail("Should throw ServiceInvokationException.");
	}
	
	@Test
	public void testSearchIngredient_invalidDataWhereIngredientNameLengthIsGreaterThan20Chars_notCallsPersistenceAndValidation() {

		IngredientSearchParam searchParam = new IngredientSearchParam(" Lorem ipsum dolor sit "); // 21 chars + 2 spaces

		// invokation
		RecipeService dietPlanService = new SimpleRecipeService(mockedRecipeRepo);
		try {
			dietPlanService.searchIngredient(searchParam);
		} catch (ServiceInvokationException e) {
			// verification - no interaction with repo
			verifyZeroInteractions(mockedRecipeRepo);
			Assert.assertEquals(1, e.getContext().getErrors().size());
			Assert.assertEquals("Enter only 20 characters in the field 'Ingredient Name'",
					e.getContext().getErrors().get(0));
			return;
		}
		Assert.fail("Should throw ServiceInvokationException.");
	}

}
