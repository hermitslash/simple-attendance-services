package trmio.inc.ams.views.attendances;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import trmio.inc.ams.data.entity.Attendance;
import trmio.inc.ams.data.service.AttendanceService;
import trmio.inc.ams.views.MainLayout;

@PageTitle("Attendances")
@Route(value = "attendances/:attendanceID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@Uses(Icon.class)
public class AttendancesView extends Div implements BeforeEnterObserver {

    private final String ATTENDANCE_ID = "attendanceID";
    private final String ATTENDANCE_EDIT_ROUTE_TEMPLATE = "attendances/%s/edit";

    private final Grid<Attendance> grid = new Grid<>(Attendance.class, false);

    private TextField studentStaffId;
    private TextField attendanceType;
    private Checkbox isPresent;
    private DateTimePicker attendanceDate;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Attendance> binder;

    private Attendance attendance;

    private final AttendanceService attendanceService;

    public AttendancesView(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
        addClassNames("attendances-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("studentStaffId").setAutoWidth(true);
        grid.addColumn("attendanceType").setAutoWidth(true);
        LitRenderer<Attendance> isPresentRenderer = LitRenderer.<Attendance>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", isPresent -> isPresent.isIsPresent() ? "check" : "minus").withProperty("color",
                        isPresent -> isPresent.isIsPresent()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(isPresentRenderer).setHeader("Is Present").setAutoWidth(true);

        grid.addColumn("attendanceDate").setAutoWidth(true);
        grid.setItems(query -> attendanceService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(ATTENDANCE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(AttendancesView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Attendance.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.attendance == null) {
                    this.attendance = new Attendance();
                }
                binder.writeBean(this.attendance);
                attendanceService.update(this.attendance);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(AttendancesView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> attendanceId = event.getRouteParameters().get(ATTENDANCE_ID).map(Long::parseLong);
        if (attendanceId.isPresent()) {
            Optional<Attendance> attendanceFromBackend = attendanceService.get(attendanceId.get());
            if (attendanceFromBackend.isPresent()) {
                populateForm(attendanceFromBackend.get());
            } else {
                Notification.show(String.format("The requested attendance was not found, ID = %s", attendanceId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(AttendancesView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        studentStaffId = new TextField("Student Staff Id");
        attendanceType = new TextField("Attendance Type");
        isPresent = new Checkbox("Is Present");
        attendanceDate = new DateTimePicker("Attendance Date");
        attendanceDate.setStep(Duration.ofSeconds(1));
        formLayout.add(studentStaffId, attendanceType, isPresent, attendanceDate);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Attendance value) {
        this.attendance = value;
        binder.readBean(this.attendance);

    }
}
