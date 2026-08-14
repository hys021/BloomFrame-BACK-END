package com.bloomframe.server.ai;

import java.util.List;

public record NewsletterContent(
        String title,
        String body,
        List<String> tips
) {}
