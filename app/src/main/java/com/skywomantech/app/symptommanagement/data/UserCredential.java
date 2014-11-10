package com.skywomantech.app.symptommanagement.data;

public class UserCredential {
	


    private String id;
    private volatile long dbId; // local storage / CP id
    private String userId;
    private String userName;
    private UserRole userRole = UserRole.NOT_ASSIGNED;
    private int userRoleValue = UserRole.NOT_ASSIGNED.getValue();
    private String password;
    private volatile long lastLogin;

    public enum UserRole {
        NOT_ASSIGNED(-1), ADMIN(500), PHYSICIAN(300), PATIENT(200);

        private int value;

        private UserRole(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
        public void setValue(int value) {this.value = value;}

        public static UserRole findByValue(int val){
            for(UserRole r : values()){
                if( r.getValue() == val ){
                    return r;
                }
            }
            return NOT_ASSIGNED;
        }
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public UserRole getUserType() {
        return userRole;
    }
    public void setUserType(UserRole userRole) {
        this.userRole = userRole;
        setUserRoleValue(userRole.getValue());
    }
    public int getUserRoleValue() {
        return userRoleValue;
    }
    public void setUserRoleValue(int userRoleValue) {
        this.userRoleValue = userRoleValue;
        setUserType(UserRole.findByValue(userRoleValue));
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		result = prime * result
				+ ((userRole == null) ? 0 : userRole.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserCredential))
			return false;
		UserCredential other = (UserCredential) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		if (userRole != other.userRole)
			return false;
		return true;
	}

    @Override
    public String toString() {
        return "UserCredential{" +
                "id='" + id + '\'' +
                ", dbId=" + dbId +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userRole=" + userRole +
                ", userRoleValue=" + userRoleValue +
                ", password='" + password + '\'' +
                ", lastLogin=" + lastLogin +
                '}';
    }
}
