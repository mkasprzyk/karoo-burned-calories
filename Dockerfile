FROM gradle:6.1.1-jdk8 as development

ENV DEBIAN_FRONTEND=noninteractive

ENV ANDROID_SDK_ROOT /opt/android-sdk-linux

RUN cd /opt \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-6858069_latest.zip -O android-commandline-tools.zip \
    && mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools \
    && unzip -q android-commandline-tools.zip -d /tmp/ \
    && mv /tmp/cmdline-tools/ ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm android-commandline-tools.zip && ls -la ${ANDROID_SDK_ROOT}/cmdline-tools/latest/

ENV PATH ${PATH}:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin

RUN yes | sdkmanager --licenses

RUN touch /root/.android/repositories.cfg

RUN yes | sdkmanager "platform-tools"

RUN yes | sdkmanager --update --channel=0

RUN yes | sdkmanager \
    "platforms;android-28" \
    "build-tools;29.0.2"

FROM development as release

WORKDIR /project
COPY . /project

RUN gradlew assembleDebug
