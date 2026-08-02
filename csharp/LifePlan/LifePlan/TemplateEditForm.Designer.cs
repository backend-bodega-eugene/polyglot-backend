using AntdUI;
using System.Xml.Linq;
using static System.Net.Mime.MediaTypeNames;

namespace LifePlan;

partial class TemplateEditForm
{
    private System.ComponentModel.IContainer components = null;

    protected override void Dispose(bool disposing)
    {
        if (disposing && components != null)
        {
            components.Dispose();
        }

        base.Dispose(disposing);
    }

    private void InitializeComponent()
    {
        lblName = new AntdUI.Label();
        txtName = new TextBox();

        lblTime = new AntdUI.Label();
        timeReminder = new DateTimePicker();

        lblDuration = new AntdUI.Label();
        numDuration = new NumericUpDown();

        lblRemark = new AntdUI.Label();
        txtRemark = new TextBox();

        btnSave = new AntdUI.Button();
        btnCancel = new AntdUI.Button();

        ((System.ComponentModel.ISupportInitialize)numDuration)
            .BeginInit();

        SuspendLayout();

        // lblName
        lblName.AutoSize = true;
        lblName.Location = new Point(30, 28);
        lblName.Name = "lblName";
        lblName.Size = new Size(74, 31);
        lblName.Text = "任务名称";

        // txtName
        txtName.Location = new Point(130, 24);
        txtName.Name = "txtName";
        txtName.Size = new Size(360, 38);

        // lblTime
        lblTime.AutoSize = true;
        lblTime.Location = new Point(30, 86);
        lblTime.Name = "lblTime";
        lblTime.Size = new Size(74, 31);
        lblTime.Text = "提醒时间";

        // timeReminder
        timeReminder.CustomFormat = "HH:mm";
        timeReminder.Format = DateTimePickerFormat.Custom;
        timeReminder.Location = new Point(130, 82);
        timeReminder.Name = "timeReminder";
        timeReminder.ShowUpDown = true;
        timeReminder.Size = new Size(160, 38);

        // lblDuration
        lblDuration.AutoSize = true;
        lblDuration.Location = new Point(30, 144);
        lblDuration.Name = "lblDuration";
        lblDuration.Size = new Size(74, 31);
        lblDuration.Text = "任务时长";

        // numDuration
        numDuration.Location = new Point(130, 140);
        numDuration.Maximum = 1440;
        numDuration.Minimum = 1;
        numDuration.Name = "numDuration";
        numDuration.Size = new Size(160, 38);
        numDuration.Value = 30;

        // lblRemark
        lblRemark.AutoSize = true;
        lblRemark.Location = new Point(30, 202);
        lblRemark.Name = "lblRemark";
        lblRemark.Size = new Size(50, 31);
        lblRemark.Text = "备注";

        // txtRemark
        txtRemark.Location = new Point(130, 198);
        txtRemark.Multiline = true;
        txtRemark.Name = "txtRemark";
        txtRemark.ScrollBars = ScrollBars.Vertical;
        txtRemark.Size = new Size(360, 130);

        // btnSave
        btnSave.Location = new Point(270, 350);
        btnSave.Name = "btnSave";
        btnSave.Size = new Size(105, 45);
        btnSave.Text = "保存";
       // btnSave.UseVisualStyleBackColor = true;
        btnSave.Click += BtnSave_Click;

        // btnCancel
        btnCancel.DialogResult = DialogResult.Cancel;
        btnCancel.Location = new Point(385, 350);
        btnCancel.Name = "btnCancel";
        btnCancel.Size = new Size(105, 45);
        btnCancel.Text = "取消";
       // btnCancel.UseVisualStyleBackColor = true;

        // TemplateEditForm
        AcceptButton = btnSave;
        AutoScaleDimensions = new SizeF(14F, 31F);
        AutoScaleMode = AutoScaleMode.Font;
        CancelButton = btnCancel;
        ClientSize = new Size(530, 425);
        Controls.Add(btnCancel);
        Controls.Add(btnSave);
        Controls.Add(txtRemark);
        Controls.Add(lblRemark);
        Controls.Add(numDuration);
        Controls.Add(lblDuration);
        Controls.Add(timeReminder);
        Controls.Add(lblTime);
        Controls.Add(txtName);
        Controls.Add(lblName);
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        MinimizeBox = false;
        Name = "TemplateEditForm";
        StartPosition = FormStartPosition.CenterParent;
        Text = "任务模板";

        ((System.ComponentModel.ISupportInitialize)numDuration)
            .EndInit();

        ResumeLayout(false);
        PerformLayout();
    }

    private AntdUI.Label lblName;
    private TextBox txtName;

    private AntdUI.Label lblTime;
    private DateTimePicker timeReminder;

    private AntdUI.Label lblDuration;
    private NumericUpDown numDuration;

    private AntdUI.Label lblRemark;
    private TextBox txtRemark;

    private AntdUI.Button btnSave;
    private AntdUI.Button btnCancel;
}