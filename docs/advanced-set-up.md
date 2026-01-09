---
layout: page
title: Advanced setup
description: Step-by-step guide for advanced setup of Überböse API
---

## Advanced setup

As described [here](https://flarn2006.blogspot.com/2014/09/hacking-bose-soundtouch-and-its-linux.html)
and [here](https://github.com/deborahgu/soundcork)
you can get a root shell into your speaker when you plug in a USB stick with an empty file called `remote_services`.

For me, it was a bit hard to figure out how the USB stick needed to be formatted,
so here is step-by-step guide to preparing your USB stick.

## Guide: Creating an EXT2 "Unlock" USB Stick

**OS:** Linux Command Line

### ⚠️ Warning

**This process deletes all data on the USB stick.**
Identify your USB drive letter carefully (e.g., `sdb`, `sdc`). 
In this guide, we use **`sdX`**. Replace `X` with your actual letter.

---

### Step 1: Identify and Unmount

Plug in the USB stick into your Linux PC and find its name.

```bash
lsblk
# Look for your stick (e.g., /dev/sdc). We will use 'sdX' below.

```

Unmount it to ensure no files are busy.

```bash
sudo umount /dev/sdX*

```

### Step 2: Create MBR Partition Table

1. Open the partition tool:
```bash
sudo fdisk /dev/sdX

```

2. Type the following letters in order (press **Enter** after each):
* `o` (Create new DOS partition table)
* `n` (New partition)
* `p` (Primary)
* `1` (Partition number 1)
* *(Press Enter)* (Default first sector)
* *(Press Enter)* (Default last sector)
* `w` (Write changes and exit)

### Step 3: Format to Legacy EXT2

Format the new partition (`sdX1`) with 128-byte inodes.

```bash
sudo mkfs.ext2 -I 128 -b 4096 /dev/sdX1

```

### Step 4: Add the Trigger File

Mount the drive and create the "key" file that activates the services.

1. Create a mount point (if not exists):
```bash
sudo mkdir -p /mnt/usb

```

2. Mount the partition:
```bash
sudo mount /dev/sdX1 /mnt/usb

```

3. Create the magic file:
```bash
sudo touch /mnt/usb/remote_services

```

4. Unmount the drive to save the data.

```bash
sudo umount /mnt/usb

```

**Done.** Plug the stick into the powered-off speaker, turn it on, and wait for the services (`sshd` / `telnetd`) to start.

## Connect via telnet

After the USB-Stick is plugged in, use your computer to open the shell.

```bash
telnet 192.168.178.2
```
Replace `192.168.178.2` with the IP address of your speaker.

You should see something like that
```
... --- ..- -. -.. - --- ..- -.-. ....

        ____  ____  _____ _________
       / __ )/ __ \/ ___// _______/
      / __  / / / /\__ \/ __/
 ____/ /_/ / /_/ /___/ / /___
/_________/\____//____/_____/


spotty login: root
```
Type `root` as username.
No password is required. 😅
