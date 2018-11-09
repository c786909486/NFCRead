package kanghong.axun.com.nfcdemo;

import java.util.List;



public class GetChildProgramResponseBean {

    /**
     * message : 成功
     * groupProgramName : 政务动态
     * status : 100
     * childProgramList : [{"programTypeName":"今日荣成","programTypeId":30101},{"programTypeName":"时政要闻","programTypeId":30102},{"programTypeName":"图片新闻","programTypeId":30103},{"programTypeName":"区镇动态","programTypeId":30104},{"programTypeName":"部门动态","programTypeId":30105},{"programTypeName":"政府文件","programTypeId":30106},{"programTypeName":"规范性文件和政策措施","programTypeId":30107},{"programTypeName":"人事任免","programTypeId":30108},{"programTypeName":"政府公报","programTypeId":30109},{"programTypeName":"政府工作报告","programTypeId":30110},{"programTypeName":"便民提示","programTypeId":30111}]
     */

    private String message;
    private String groupProgramName;
    private int status;
    private List<ChildProgramListBean> childProgramList;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGroupProgramName() {
        return groupProgramName;
    }

    public void setGroupProgramName(String groupProgramName) {
        this.groupProgramName = groupProgramName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<ChildProgramListBean> getChildProgramList() {
        return childProgramList;
    }

    public void setChildProgramList(List<ChildProgramListBean> childProgramList) {
        this.childProgramList = childProgramList;
    }

    public static class ChildProgramListBean {
        /**
         * programTypeName : 今日荣成
         * programTypeId : 30101
         */

        private String programTypeName;
        private int programTypeId;

        public String getProgramTypeName() {
            return programTypeName;
        }

        public void setProgramTypeName(String programTypeName) {
            this.programTypeName = programTypeName;
        }

        public int getProgramTypeId() {
            return programTypeId;
        }

        public void setProgramTypeId(int programTypeId) {
            this.programTypeId = programTypeId;
        }
    }
}
