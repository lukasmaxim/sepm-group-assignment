package at.ac.tuwien.sepm.assignment.groupphase.application.ui;

import at.ac.tuwien.sepm.assignment.groupphase.application.dto.Recipe;
import at.ac.tuwien.sepm.assignment.groupphase.application.service.RecipeService;
import at.ac.tuwien.sepm.assignment.groupphase.application.service.ServiceInvokationException;
import at.ac.tuwien.sepm.assignment.groupphase.application.util.implementation.SpringFXMLLoader;
import at.ac.tuwien.sepm.assignment.groupphase.application.util.implementation.UserInterfaceUtility;
import at.ac.tuwien.sepm.assignment.groupphase.main.MainApplication;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;

import static at.ac.tuwien.sepm.assignment.groupphase.application.util.implementation.UserInterfaceUtility.showAlert;

@Controller
public class TabRecipesController {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private RecipeService recipeService;

	@FXML
	Button addRecipeButton;

	@FXML
	TableView<Recipe> recipeTableView;

	@FXML
	TableColumn<Recipe, String> nameTableColumn;

	@FXML
	TableColumn<Recipe, Integer> caloriesTableColumn;

	@FXML
	TableColumn<Recipe, Integer> carbohydratesTableColumn;

	@FXML
	TableColumn<Recipe, Integer> proteinsTableColumn;

	@FXML
	TableColumn<Recipe, Integer> fatsTableColumn;

	@FXML
	TableColumn<Recipe, Double> preparationTimeTableColumn;

	@FXML
	private ObservableList<Recipe> recipeObservableList = FXCollections.observableArrayList();

	public TabRecipesController(RecipeService recipeService) {
		this.recipeService = recipeService;
	}

	@FXML
	public void initialize() {
		recipeTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		caloriesTableColumn.setCellValueFactory(
				x -> new SimpleIntegerProperty((int) Math.ceil(x.getValue().getCalories())).asObject());
		carbohydratesTableColumn.setCellValueFactory(
				x -> new SimpleIntegerProperty((int) Math.ceil(x.getValue().getCarbohydrates())).asObject());
		proteinsTableColumn.setCellValueFactory(
				(x -> new SimpleIntegerProperty((int) Math.ceil(x.getValue().getProteins())).asObject()));
		fatsTableColumn.setCellValueFactory(
				x -> new SimpleIntegerProperty((int) Math.ceil(x.getValue().getFats())).asObject());
		preparationTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

		recipeTableView.setRowFactory(tableView -> {
			final TableRow<Recipe> row = new TableRow<>();

			final ContextMenu recipeContextMenu = new ContextMenu();
			final MenuItem editMenuItem = new MenuItem("Edit");
			editMenuItem.setOnAction(event -> onEditRecipeClicked(row.getItem()));
			recipeContextMenu.getItems().add(editMenuItem);

			row.contextMenuProperty()
					.bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(recipeContextMenu));
			return row;
		});

		updateRecipeTableView();

	}

	private void onEditRecipeClicked(Recipe recipe) {
		LOG.info("Edit recipe button clicked");
		loadExternalController("/fxml/RecipeDetails.fxml", "Edit Recipe", recipe);
		updateRecipeTableView();
	}

	@FXML
	public void onAddRecipeButtonClicked(ActionEvent actionEvent) {
		LOG.info("Add recipe button clicked");
		loadExternalController("/fxml/RecipeDetails.fxml", "Add Recipe", null);
		updateRecipeTableView();
	}

	private void loadExternalController(String Path, String Title, Recipe recipe) {
		try {
			final var fxmlLoader = MainApplication.context.getBean(SpringFXMLLoader.class);
			URL location = getClass().getResource(Path);
			fxmlLoader.setLocation(location);
			Stage stage = new Stage();

			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(addRecipeButton.getScene().getWindow());
			stage.setTitle(Title);

			var load = fxmlLoader.loadAndWrap(getClass().getResourceAsStream(Path), RecipeController.class);
			load.getController().initializeView(recipe);
			stage.setScene(new Scene((Parent) load.getLoadedObject()));

			stage.showAndWait();
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}

	}

	private void updateRecipeTableView() {
		recipeObservableList.clear();
		try {
			recipeObservableList.addAll(recipeService.getRecipes());
		} catch (ServiceInvokationException e) {
			UserInterfaceUtility.handleFaults(e.getContext());
		}

		recipeTableView.setItems(recipeObservableList);
	}
}