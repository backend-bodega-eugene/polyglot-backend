package cmd

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
)

var rootCmd = &cobra.Command{
	Use:   "app",
	Short: "A demo CLI app",
	Long:  "app 是一个用 Cobra 做的命令行示例工具。",
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("欢迎使用 app！试试 `app hello --name LaoHu`")
	},
}

func Execute() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}
