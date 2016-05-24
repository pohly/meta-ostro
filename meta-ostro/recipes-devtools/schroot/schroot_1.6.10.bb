SUMMARY = "Execute commands or interactive shells in different chroots"
HOMEPAGE = "https://tracker.debian.org/pkg/schroot"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=5c520cebfab3c2fcc01082a9f91b8595"

SRC_URI = "http://http.debian.net/debian/pool/main/s/schroot/schroot_${PV}.orig.tar.xz file://stat-dev-inode"
SRC_URI[md5sum] = "f8ec667831487f4b12e094bc0dc9bbe3"
SRC_URI[sha256sum] = "3ce8dfd9cb97b099e4b6d4ccec421d6cc8c9ef84574681e928a12badb5643d0b"

DEPENDS = "util-linux boost libpam"
RDEPENDS_${PN} = "dpkg perl"

inherit cmake gettext

# Several test programs get run to check for broken behavior.
# We have to set the result of these tests in advance.
# std::regex is only available when compiling in C++11 mode,
# which is not enabled yet by default, so we just use Boost.
EXTRA_OECMAKE = "-DHAVE_REGEX_REGEX_EXITCODE=1 -DHAVE_BOOST_REGEX_EXITCODE=0 -DCPPUNIT_CLASS_EXITCODE=0"

# Simplify the build by not producing man pages. Would depend on po4a.
do_patch[postfuncs] += "no_man_pages "
no_man_pages () {
    sed -i -e 's/^add_subdirectory(man)/# add_subdirectory(man)/' ${S}/CMakeLists.txt
}

do_install_append () {
    # @include not supported, must use "auth include common-auth".
    sed -i -e 's/^@include common-\(.*\)/\1 include common-\1/' ${D}${sysconfdir}/pam.d/schroot

    # Busybox stat does not support --format. Use custom shell function instead.
    sed -i -e 's;/usr/bin/stat --format="%d %i";statdevinode;g' ${D}${sysconfdir}/schroot/setup.d/*
    cat >>${D}${datadir}/schroot/setup/common-functions ${WORKDIR}/stat-dev-inode

    # Running services not supported.
    rm ${D}${sysconfdir}/schroot/setup.d/70services

    # There's no /etc/networks.
    sed -i -e 's/networks/# networks/' ${D}${sysconfdir}/schroot/default/nssdatabases
}
