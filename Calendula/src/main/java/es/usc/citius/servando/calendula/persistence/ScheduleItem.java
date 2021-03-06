package es.usc.citius.servando.calendula.persistence;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import es.usc.citius.servando.calendula.scheduling.DailyAgenda;

/**
 * Created by joseangel.pineiro on 7/9/14.
 */
@Table(name = "ScheduleItems", id = ScheduleItem.COLUMN_ID)
public class ScheduleItem extends Model {
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCHEDULE = "Schedule";
    public static final String COLUMN_ROUTINE = "Routine";
    public static final String COLUMN_DOSE = "Dose";

    @Column(name = COLUMN_SCHEDULE, onDelete = Column.ForeignKeyAction.NO_ACTION, onUpdate = Column.ForeignKeyAction.NO_ACTION)
    private Schedule schedule;


    @Column(name = COLUMN_ROUTINE, onDelete = Column.ForeignKeyAction.NO_ACTION, onUpdate = Column.ForeignKeyAction.NO_ACTION)
    private Routine routine;

    @Column(name = COLUMN_DOSE)
    private float dose;

    public ScheduleItem() {
        super();
    }

    public ScheduleItem(Schedule schedule, Routine routine, float dose) {
        this();
        this.schedule = schedule;
        this.routine = routine;
        this.dose = dose;
    }

    public ScheduleItem(Schedule schedule, Routine routine) {
        this();
        this.schedule = schedule;
        this.routine = routine;
    }

    public void saveAndUpdateDailyAgenda() {
        this.save();
        DailyAgenda.instance().updateDailySchedule(this);
    }

    public Routine routine() {
        return routine;
    }

    public void setRoutine(Routine routine) {
        this.routine = routine;
    }

    public Schedule schedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public float dose() {
        return dose;
    }

    public String displayDose() {
        int integerPart = (int) dose;
        double fraction = dose - integerPart;

        String fractionRational;
        if (fraction == 0.125)
            fractionRational = "1/8";
        else if (fraction == 0.25)
            fractionRational = "1/4";
        else if (fraction == 0.5)
            fractionRational = "1/2";
        else if (fraction == 0.75)
            fractionRational = "3/4";
        else if (fraction == 0)
            return "" + ((int) dose);
        else
            return "" + dose;
        return integerPart + "+" + fractionRational;

    }

    public void setDose(float dose) {
        this.dose = dose;
    }


    public void deleteCascade() {
        DailyScheduleItem item = DailyScheduleItem.findByScheduleItem(this);
        item.delete();
        this.delete();
    }

}
