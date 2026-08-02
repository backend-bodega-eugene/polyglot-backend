using System.Xml.Linq;

namespace LifePlan;

partial class TemplateManagerControl
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
        topPanel = new Panel();
        txtKeyword = new TextBox();
        btnSearch = new Button();
        btnReset = new Button();
        btnAdd = new Button();

        gridTemplates = new DataGridView();

        colName = new DataGridViewTextBoxColumn();
        colReminderTime = new DataGridViewTextBoxColumn();
        colDuration = new DataGridViewTextBoxColumn();
        colRemark = new DataGridViewTextBoxColumn();
        colEdit = new DataGridViewButtonColumn();
        colDelete = new DataGridViewButtonColumn();

        topPanel.SuspendLayout();

        ((System.ComponentModel.ISupportInitialize)gridTemplates)
            .BeginInit();

        SuspendLayout();

        // topPanel
        topPanel.Controls.Add(btnAdd);
        topPanel.Controls.Add(btnReset);
        topPanel.Controls.Add(btnSearch);
        topPanel.Controls.Add(txtKeyword);
        topPanel.Dock = DockStyle.Top;
        topPanel.Location = new Point(0, 0);
        topPanel.Name = "topPanel";
        topPanel.Padding = new Padding(15);
        topPanel.Size = new Size(1000, 72);

        // txtKeyword
        txtKeyword.Location = new Point(15, 17);
        txtKeyword.Name = "txtKeyword";
        txtKeyword.PlaceholderText = "输入任务名称";
        txtKeyword.Size = new Size(320, 38);
        txtKeyword.KeyDown += TxtKeyword_KeyDown;

        // btnSearch
        btnSearch.Location = new Point(350, 15);
        btnSearch.Name = "btnSearch";
        btnSearch.Size = new Size(100, 42);
        btnSearch.Text = "查询";
        btnSearch.UseVisualStyleBackColor = true;
        btnSearch.Click += BtnSearch_Click;

        // btnReset
        btnReset.Location = new Point(460, 15);
        btnReset.Name = "btnReset";
        btnReset.Size = new Size(100, 42);
        btnReset.Text = "重置";
        btnReset.UseVisualStyleBackColor = true;
        btnReset.Click += BtnReset_Click;

        // btnAdd
        btnAdd.Anchor = AnchorStyles.Top | AnchorStyles.Right;
        btnAdd.Location = new Point(865, 15);
        btnAdd.Name = "btnAdd";
        btnAdd.Size = new Size(120, 42);
        btnAdd.Text = "新增任务";
        btnAdd.UseVisualStyleBackColor = true;
        btnAdd.Click += BtnAdd_Click;

        // gridTemplates
        gridTemplates.AllowUserToAddRows = false;
        gridTemplates.AllowUserToDeleteRows = false;
        gridTemplates.AllowUserToResizeRows = false;
        gridTemplates.AutoSizeColumnsMode =
            DataGridViewAutoSizeColumnsMode.Fill;
        gridTemplates.BackgroundColor = Color.White;
        gridTemplates.BorderStyle = BorderStyle.None;
        gridTemplates.ColumnHeadersHeight = 46;
        gridTemplates.Columns.AddRange(
            colName,
            colReminderTime,
            colDuration,
            colRemark,
            colEdit,
            colDelete);

        gridTemplates.Dock = DockStyle.Fill;
        gridTemplates.Location = new Point(0, 72);
        gridTemplates.MultiSelect = false;
        gridTemplates.Name = "gridTemplates";
        gridTemplates.ReadOnly = true;
        gridTemplates.RowHeadersVisible = false;
        gridTemplates.RowTemplate.Height = 46;
        gridTemplates.SelectionMode =
            DataGridViewSelectionMode.FullRowSelect;
        gridTemplates.Size = new Size(1000, 528);
        gridTemplates.CellContentClick +=
            GridTemplates_CellContentClick;

        // colName
        colName.DataPropertyName = "Name";
        colName.FillWeight = 130F;
        colName.HeaderText = "任务名称";
        colName.Name = "colName";
        colName.ReadOnly = true;

        // colReminderTime
        colReminderTime.DataPropertyName = "ReminderTimeText";
        colReminderTime.FillWeight = 80F;
        colReminderTime.HeaderText = "提醒时间";
        colReminderTime.Name = "colReminderTime";
        colReminderTime.ReadOnly = true;

        // colDuration
        colDuration.DataPropertyName = "DurationText";
        colDuration.FillWeight = 80F;
        colDuration.HeaderText = "时长";
        colDuration.Name = "colDuration";
        colDuration.ReadOnly = true;

        // colRemark
        colRemark.DataPropertyName = "Remark";
        colRemark.FillWeight = 180F;
        colRemark.HeaderText = "备注";
        colRemark.Name = "colRemark";
        colRemark.ReadOnly = true;

        // colEdit
        colEdit.FillWeight = 60F;
        colEdit.HeaderText = "操作";
        colEdit.Name = "colEdit";
        colEdit.ReadOnly = true;
        colEdit.Text = "修改";
        colEdit.UseColumnTextForButtonValue = true;

        // colDelete
        colDelete.FillWeight = 60F;
        colDelete.HeaderText = "";
        colDelete.Name = "colDelete";
        colDelete.ReadOnly = true;
        colDelete.Text = "删除";
        colDelete.UseColumnTextForButtonValue = true;

        // TemplateManagerControl
        AutoScaleDimensions = new SizeF(14F, 31F);
        AutoScaleMode = AutoScaleMode.Font;
        BackColor = Color.White;
        Controls.Add(gridTemplates);
        Controls.Add(topPanel);
        Name = "TemplateManagerControl";
        Size = new Size(1000, 600);

        topPanel.ResumeLayout(false);
        topPanel.PerformLayout();

        ((System.ComponentModel.ISupportInitialize)gridTemplates)
            .EndInit();

        ResumeLayout(false);
    }

    private Panel topPanel;

    private TextBox txtKeyword;
    private Button btnSearch;
    private Button btnReset;
    private Button btnAdd;

    private DataGridView gridTemplates;

    private DataGridViewTextBoxColumn colName;
    private DataGridViewTextBoxColumn colReminderTime;
    private DataGridViewTextBoxColumn colDuration;
    private DataGridViewTextBoxColumn colRemark;

    private DataGridViewButtonColumn colEdit;
    private DataGridViewButtonColumn colDelete;
}