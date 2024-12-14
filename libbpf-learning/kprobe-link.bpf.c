/**
* 用于监测和捕获在 Linux 内核中执行的 unlink 系统调用。unlink 系统调用的功能是删除一个文件
* 通过使用 kprobe（内核探针）在 do_unlinkat 函数的入口和退出处放置钩子，实现对该系统调用的跟踪
*/
#include "vmlinux.h"
#include <bpf/bpf_helpers.h>
#include <bpf/bpf_tracing.h>
#include <bpf/bpf_core_read.h>

char LICENSE[] SEC("license") = "Dual BSD/GPL";

/**
* 定义一个名为BPF_KPROBE(do_unlinkat)的 kprobe，当进入do_unlinkat函数时，它会被触发。
该函数接受两个参数：dfd（文件描述符）和name（文件名结构体指针）
在这个 kprobe 中，获取当前进程的 PID（进程标识符），然后读取文件名。最后使用bpf_printk函数在内核日志中打印 PID 和文件名
*/
SEC("kprobe/do_unlinkat")
int BPF_KPROBE(do_unlinkat, int dfd, struct filename *name)
{
    pid_t pid;
    const char *filename;

    pid = bpf_get_current_pid_tgid() >> 32;
    filename = BPF_CORE_READ(name, name);
    bpf_printk("KPROBE ENTRY pid = %d, filename = %s\n", pid, filename);
    return 0;
}

/**
* 定义一个名为BPF_KRETPROBE(do_unlinkat_exit)的 kretprobe
  当从do_unlinkat函数退出时，它会被触发。这个 kretprobe 的目的是捕获函数的返回值（ret）。
  再次获取当前进程的 PID，并使用bpf_printk函数在内核日志中打印 PID 和返回值
*/
SEC("kretprobe/do_unlinkat")
int BPF_KRETPROBE(do_unlinkat_exit, long ret)
{
    pid_t pid;

    pid = bpf_get_current_pid_tgid() >> 32;
    bpf_printk("KPROBE EXIT: pid = %d, ret = %ld\n", pid, ret);
    return 0;
}