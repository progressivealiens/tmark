package com.trackkers.tmark.webApi;

import java.util.List;

public class ApiResponseOperations {

    private String status;
    private String msg;
    private String checkInTime;
    private String routeId;
    private String routeName;
    private String startImageName;
    private boolean isCheckedIn;

    private List<DataBean> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getStartImageName() {
        return startImageName;
    }

    public void setStartImageName(String startImageName) {
        this.startImageName = startImageName;
    }

    public boolean isIsCheckedIn() {
        return isCheckedIn;
    }

    public void setIsCheckedIn(boolean isCheckedIn) {
        this.isCheckedIn = isCheckedIn;
    }

    public static class DataBean {

        private String routeName;
        private int routeId;
        private String routeStartAddress;
        private String routeEndAddress;
        private String routeCode;
        private String siteName;
        private String siteCode;
        private List<EmployeesBean> employees;

        public String getRouteName() {
            return routeName;
        }

        public void setRouteName(String routeName) {
            this.routeName = routeName;
        }

        public int getRouteId() {
            return routeId;
        }

        public void setRouteId(int routeId) {
            this.routeId = routeId;
        }

        public String getRouteStartAddress() {
            return routeStartAddress;
        }

        public void setRouteStartAddress(String routeStartAddress) {
            this.routeStartAddress = routeStartAddress;
        }

        public String getRouteEndAddress() {
            return routeEndAddress;
        }

        public void setRouteEndAddress(String routeEndAddress) {
            this.routeEndAddress = routeEndAddress;
        }

        public String getRouteCode() {
            return routeCode;
        }

        public void setRouteCode(String routeCode) {
            this.routeCode = routeCode;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public String getSiteCode() {
            return siteCode;
        }

        public void setSiteCode(String siteCode) {
            this.siteCode = siteCode;
        }

        public List<EmployeesBean> getEmployees() {
            return employees;
        }

        public void setEmployees(List<EmployeesBean> employees) {
            this.employees = employees;
        }

        public static class EmployeesBean {

            private String employeeName;
            private int employeeId;
            private String empCode;

            public String getEmpCode() {
                return empCode;
            }

            public void setEmpCode(String empCode) {
                this.empCode = empCode;
            }

            public String getEmployeeName() {
                return employeeName;
            }

            public void setEmployeeName(String employeeName) {
                this.employeeName = employeeName;
            }

            public int getEmployeeId() {
                return employeeId;
            }

            public void setEmployeeId(int employeeId) {
                this.employeeId = employeeId;
            }
        }
    }
}
