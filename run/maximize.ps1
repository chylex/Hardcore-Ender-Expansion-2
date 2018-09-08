Param([Parameter(Mandatory = $True, Position = 1)][string] $processId)
Add-Type -MemberDefinition '[DllImport("user32.dll")]public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);' -Name Functions -Namespace Win32
[Win32.Functions]::ShowWindow((Get-Process -Id $processId).MainWindowHandle, 3)
