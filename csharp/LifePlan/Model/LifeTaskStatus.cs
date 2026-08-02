using System.ComponentModel;

namespace Model;

public enum LifeTaskStatus
{
    [Description("未完成")]
    Pending = 0,

    [Description("完成10%")]
    Progress10 = 10,

    [Description("完成30%")]
    Progress30 = 30,

    [Description("完成50%")]
    Progress50 = 50,

    [Description("完成70%")]
    Progress70 = 70,

    [Description("完成90%")]
    Progress90 = 90,

    [Description("已完成")]
    Completed = 100
}
