package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
)

var name string

var helloCmd = &cobra.Command{
	Use:   "hello",
	Short: "打个招呼",
	Run: func(cmd *cobra.Command, args []string) {
		if name == "" {
			name = "世界"
		}
		fmt.Printf("你好，%s！\n", name)
	},
}

func init() {
	rootCmd.AddCommand(helloCmd)
	helloCmd.Flags().StringVarP(&name, "name", "n", "", "名字")
}
