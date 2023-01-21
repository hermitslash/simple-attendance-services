package trmio.inc.ams.data.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import java.time.LocalDateTime;

@Entity
public class Attendance extends AbstractEntity {

    @Email
    private String studentStaffId;
    private String attendanceType;
    private boolean isPresent;
    private LocalDateTime attendanceDate;

    public String getStudentStaffId() {
        return studentStaffId;
    }
    public void setStudentStaffId(String studentStaffId) {
        this.studentStaffId = studentStaffId;
    }
    public String getAttendanceType() {
        return attendanceType;
    }
    public void setAttendanceType(String attendanceType) {
        this.attendanceType = attendanceType;
    }
    public boolean isIsPresent() {
        return isPresent;
    }
    public void setIsPresent(boolean isPresent) {
        this.isPresent = isPresent;
    }
    public LocalDateTime getAttendanceDate() {
        return attendanceDate;
    }
    public void setAttendanceDate(LocalDateTime attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

}
