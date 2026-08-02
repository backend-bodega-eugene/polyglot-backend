using AntdUI;
using Model;
using System.Xml.Linq;

namespace LifePlan;

public partial class TemplateEditForm : BaseForm
{
    public TaskTemplate Template { get; }

    public TemplateEditForm(TaskTemplate? template = null)
    {
        InitializeComponent();

        Template = template ?? new TaskTemplate();

        if (template != null)
        {
            Text = "修改任务模板";

            txtName.Text = template.Name;

            timeReminder.Value = DateTime.Today
                .Add(template.ReminderTime.ToTimeSpan());

            numDuration.Value = template.DurationMinutes;

            txtRemark.Text = template.Remark ?? string.Empty;
        }
        else
        {
            Text = "新增任务模板";
            timeReminder.Value = DateTime.Today.AddHours(9);
        }
    }

    private void BtnSave_Click(
        object? sender,
        EventArgs e)
    {
        var name = txtName.Text.Trim();

        if (string.IsNullOrWhiteSpace(name))
        {
            MessageBox.Show(
                "请输入任务名称。",
                "提示",
                MessageBoxButtons.OK,
                MessageBoxIcon.Warning);

            txtName.Focus();
            return;
        }

        Template.Name = name;

        Template.ReminderTime = TimeOnly.FromDateTime(
            timeReminder.Value);

        Template.DurationMinutes = Convert.ToInt32(
            numDuration.Value);

        Template.Remark = txtRemark.Text.Trim();

        DialogResult = DialogResult.OK;
        Close();
    }
}