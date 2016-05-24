.. _debian-on-ostro-os:

Installing and Running Debian Binaries on Ostro |trade| OS
##########################################################

Like any other Linux distribution, Ostro OS provides only very limited
binary compatibility with other distributions. Binaries compiled for
other distributions may or may not run without recompilation,
depending on how similar the libraries are that were used when
compiling the binary.

However, it is possible to install a minimal Debian root filesystem in
a chroot on Ostro OS and then install and use the original Debian
packages unmodified inside a chroot environment.

This document explains how to do that with the ``debootstrap`` tool
(for creating the Debian root filesystem) and the ``schroot`` tool
(for running commands). The same instructions also work for Ubuntu.

Installing the Tools
====================

``debootstrap`` and ``schroot`` are not included by default in Ostro
images. Before using them, one has to include them in a customized
image. For example, to include them in ``ostro-image-noswupd`` images,
add the following to ``local.conf``::

  OSTRO_IMAGE_NOSWUPD_EXTRA_INSTALL_append = " debootstrap schroot"

Setting up a Root Filesystem
============================

After booting an image with the tools installed, choose a Debian or Ubuntu
release (``wheezy`` in the following example), and a directory where the
root filesystem will be created (``/var/debian-wheezy`` here), then invoke::

  debootstrap --variant=minbase wheezy /var/debian-wheezy

It is possible to customize the installation, for example by choosing
a more complete installation for on-target compilation with
``--variant=buildd``. See the `debootstrap man page`_ for details.

.. _debootstrap man page: https://manpages.debian.org/cgi-bin/man.cgi?sektion=8&query=debootstrap&apropos=0&manpath=testing&locale=en

Using the Root Filesystem
=========================

The ``schroot`` can be used to run commands transparently inside the
new root filesystem. It is possible to run single commands as well as
work interactively in a shell.

``schroot`` supports more than one chroot. Each of them has to be
configured under ``/etc/schroot/chroot.d``. One file per chroot is
recommended. For the example above, create the file
``/etc/schroot/chroot.d/debian-wheezy`` with the following content::

  [debian-wheezy]
  type=directory
  directory=/var/debian-wheezy

The `schroot configuration man page`_ explains all available
options. When set up like this, the chroot environment shares the
`/home/` directories with the host OS.

Once this configuration is in place, the chroot can be used, as
explained in the `schroot man page`_.  Here are some examples::

  root@qemux86:~# schroot -l
  chroot:debian-wheezy
  root@qemux86:~# schroot -c debian-wheezy -- echo hello world
  hello world
  root@qemux86:~# schroot -c debian-wheezy -- bash
  (debian-wheezy)root@qemux86:~# tree -L 1 /
  bash: tree: command not found
  (debian-wheezy)root@qemux86:~# apt-get update
  Hit http://ftp.us.debian.org wheezy Release.gpg
  Hit http://ftp.us.debian.org wheezy Release
  Hit http://ftp.us.debian.org wheezy/main i386 Packages
  Hit http://ftp.us.debian.org wheezy/main Translation-en
  Reading package lists... Done
  (debian-wheezy)root@qemux86:~# apt-get install tree
  Reading package lists... Done
  Building dependency tree
  Reading state information... Done
  The following NEW packages will be installed:
    tree
  0 upgraded, 1 newly installed, 0 to remove and 0 not upgraded.
  Need to get 42.0 kB of archives.
  After this operation, 110 kB of additional disk space will be used.
  Get:1 http://ftp.us.debian.org/debian/ wheezy/main tree i386 1.6.0-1 [42.0 kB]
  Fetched 42.0 kB in 0s (138 kB/s)
  debconf: delaying package configuration, since apt-utils is not installed
  Selecting previously unselected package tree.
  (Reading database ... 10852 files and directories currently installed.)
  Unpacking tree (from .../archives/tree_1.6.0-1_i386.deb) ...
  Setting up tree (1.6.0-1) ...
  (debian-wheezy)root@qemux86:~# tree -L 1 /
  /
  |-- bin
  |-- boot
  |-- debootstrap
  |-- dev
  |-- etc
  |-- home
  |-- lib
  |-- media
  |-- mnt
  |-- opt
  |-- proc
  |-- root
  |-- run
  |-- sbin
  |-- selinux
  |-- srv
  |-- sys
  |-- tmp
  |-- usr
  `-- var
  
  20 directories, 0 files

.. _schroot man page: https://manpages.debian.org/cgi-bin/man.cgi?sektion=0&query=schroot&apropos=0&manpath=testing&locale=en
.. _schroot config man page: https://manpages.debian.org/cgi-bin/man.cgi?sektion=0&query=schroot.conf&apropos=0&manpath=testing&locale=en

Limitations
===========

Installing packages and running services which depend on special users
in ``/etc/passwd`` is likely to fail. As set up above, ``/etc/passwd``
from Ostro OS is used also inside the chroot (because otherwise home
directories would not appear with the right owner), but
Debian-specific users are not defined in Ostro OS.
