FROM ubuntu:focal

LABEL org.opencontainers.image.authors="henric@hawaii.edu"

# compiler, etc.
RUN apt-get update
RUN apt-get -y install gcc 
RUN apt-get -y install cmake
RUN apt-get -y install sudo

# install libjpeg for modern c++
RUN apt-get -y install libjpeg-dev

# ics432 user
RUN useradd -ms /bin/bash ics432
RUN adduser ics432 sudo
RUN echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers

USER ics432
WORKDIR /home/ics432
ENV CC="gcc"

# c_filters code
COPY --chown=ics432 c_filters /home/ics432/c_filters
RUN cd c_filters && mkdir build && cd build && cmake .. && make && sudo make install 

# input / output dirs
RUN mkdir /tmp/input/
RUN mkdir /tmp/output/
