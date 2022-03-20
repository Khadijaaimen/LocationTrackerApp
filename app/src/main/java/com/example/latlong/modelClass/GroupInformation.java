package com.example.latlong.modelClass;

public class GroupInformation {
    String groupName, groupIcon;
    Integer memberCount, groupNumber;

    public GroupInformation() {
    }

    public GroupInformation(String groupName, String groupIcon, Integer memberCount, Integer groupNumber) {
        this.groupName = groupName;
        this.groupIcon = groupIcon;
        this.memberCount = memberCount;
        this.groupNumber = groupNumber;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(Integer groupNumber) {
        this.groupNumber = groupNumber;
    }
}
